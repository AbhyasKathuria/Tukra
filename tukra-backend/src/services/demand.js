import { query, toCamelCase } from '../db.js';

/**
 * Calculates demand forecast for an outlet on a target date:
 * - Averages same weekday over the last 4 weeks from collected orders and plate sessions
 * - Adds current pre-reservations already booked
 */
export async function getDemandForecast(outletId, targetDateStr = null) {
  // If targetDateStr is not passed, default to tomorrow in IST
  let targetDate;
  if (targetDateStr) {
    targetDate = new Date(targetDateStr);
  } else {
    targetDate = new Date();
    targetDate.setDate(targetDate.getDate() + 1);
  }

  const dayOfWeek = targetDate.getDay(); // 0 is Sunday, 1 is Monday, etc.
  const dateFormatted = targetDate.toISOString().slice(0, 10);

  // 1. Fetch all available menu items for this outlet
  const itemsRes = await query(
    `SELECT m.id, m.name, m.price_paise, m.is_veg, c.name as category_name
       FROM menu_items m
  LEFT JOIN categories c ON c.id = m.category_id
      WHERE m.outlet_id = $1 AND m.is_available = true
      ORDER BY m.name ASC`,
    [outletId]
  );

  const forecast = [];

  for (const item of itemsRes.rows) {
    // A. Historical 4-week average for same weekday from orders
    const historyRes = await query(
      `SELECT COALESCE(SUM(oi.qty), 0) as total_qty
         FROM order_items oi
         JOIN orders o ON o.id = oi.order_id
        WHERE oi.menu_item_id = $1
          AND o.outlet_id = $2
          AND o.status IN ('COLLECTED', 'READY')
          AND EXTRACT(DOW FROM o.created_at) = $3
          AND o.created_at >= now() - interval '28 days'`,
      [item.id, outletId, dayOfWeek]
    );

    // B. Historical 4-week average from plate items
    const plateHistoryRes = await query(
      `SELECT COALESCE(SUM(pi.qty), 0) as total_qty
         FROM plate_items pi
         JOIN plate_sessions ps ON ps.id = pi.plate_session_id
        WHERE pi.menu_item_id = $1
          AND ps.outlet_id = $2
          AND ps.status = 'PAID'
          AND EXTRACT(DOW FROM pi.served_at) = $3
          AND pi.served_at >= now() - interval '28 days'`,
      [item.id, outletId, dayOfWeek]
    );

    const totalSoldPast4Weeks =
      parseInt(historyRes.rows[0].total_qty, 10) + parseInt(plateHistoryRes.rows[0].total_qty, 10);
    const avgHistoricalWeekday = Math.round(totalSoldPast4Weeks / 4);

    // C. Current reservations for this date
    const resCountRes = await query(
      `SELECT COUNT(mr.id) as reservation_count, COALESCE(mm.planned_qty, 0) as planned_qty
         FROM mess_menus mm
    LEFT JOIN mess_reservations mr ON mr.mess_menu_id = mm.id
        WHERE mm.outlet_id = $1
          AND mm.menu_item_id = $2
          AND mm.service_date = $3
        GROUP BY mm.id, mm.planned_qty`,
      [outletId, item.id, dateFormatted]
    );

    const currentReservations =
      resCountRes.rows.length > 0 ? parseInt(resCountRes.rows[0].reservation_count, 10) : 0;
    const plannedQty =
      resCountRes.rows.length > 0 ? parseInt(resCountRes.rows[0].planned_qty, 10) : null;

    // Estimate: baseline historical average + direct pre-reservations (min 15 units if new)
    const suggestedQty = Math.max(15, avgHistoricalWeekday + currentReservations);

    forecast.push({
      menuItemId: item.id,
      name: item.name,
      category: item.category_name || 'General',
      isVeg: item.is_veg,
      fourWeekWeekdayAvg: avgHistoricalWeekday,
      currentReservations,
      plannedQty,
      suggestedQty,
      confidenceLabel: 'Estimate'
    });
  }

  return {
    outletId,
    targetDate: dateFormatted,
    dayOfWeek: targetDate.toLocaleDateString('en-US', { weekday: 'long' }),
    items: forecast
  };
}

/**
 * Calculates high-level admin KPI metrics for the campus dashboard
 */
export async function getAdminOverview(campusId) {
  // 1. Orders and Revenue Today
  const todayRes = await query(
    `SELECT COUNT(o.id) as order_count,
            COALESCE(SUM(o.total_paise), 0) as revenue_paise
       FROM orders o
       JOIN outlets outl ON outl.id = o.outlet_id
      WHERE outl.campus_id = $1
        AND o.status IN ('PLACED', 'ACCEPTED', 'PREPARING', 'READY', 'COLLECTED')
        AND o.created_at >= (date_trunc('day', now() AT TIME ZONE 'Asia/Kolkata') AT TIME ZONE 'Asia/Kolkata')`,
    [campusId]
  );

  // 2. Active Outlets Count
  const outletRes = await query(
    `SELECT COUNT(id) as total_outlets,
            COUNT(CASE WHEN is_open = true THEN 1 END) as active_outlets
       FROM outlets
      WHERE campus_id = $1`,
    [campusId]
  );

  // 3. Average wait time in minutes today (difference between slot start and order placed)
  const waitRes = await query(
    `SELECT COALESCE(AVG(EXTRACT(EPOCH FROM (s.starts_at - o.created_at))/60), 8) as avg_wait_minutes
       FROM orders o
       JOIN slots s ON s.id = o.slot_id
       JOIN outlets outl ON outl.id = o.outlet_id
      WHERE outl.campus_id = $1
        AND o.created_at >= (date_trunc('day', now() AT TIME ZONE 'Asia/Kolkata') AT TIME ZONE 'Asia/Kolkata')`,
    [campusId]
  );

  // 4. Orders by hour today (08:00 to 20:00)
  const hourlyRes = await query(
    `SELECT EXTRACT(HOUR FROM o.created_at AT TIME ZONE 'Asia/Kolkata') as hour,
            COUNT(o.id) as count,
            COALESCE(SUM(o.total_paise), 0) as revenue_paise
       FROM orders o
       JOIN outlets outl ON outl.id = o.outlet_id
      WHERE outl.campus_id = $1
        AND o.created_at >= (date_trunc('day', now() AT TIME ZONE 'Asia/Kolkata') AT TIME ZONE 'Asia/Kolkata')
      GROUP BY hour
      ORDER BY hour ASC`,
    [campusId]
  );

  const hourlyMap = new Map();
  for (const row of hourlyRes.rows) {
    hourlyMap.set(parseInt(row.hour, 10), {
      count: parseInt(row.count, 10),
      revenuePaise: parseInt(row.revenue_paise, 10)
    });
  }

  const hourlyBreakdown = [];
  for (let h = 8; h <= 20; h++) {
    const data = hourlyMap.get(h) || { count: 0, revenuePaise: 0 };
    hourlyBreakdown.push({
      hour: h,
      label: `${h % 12 || 12} ${h >= 12 ? 'PM' : 'AM'}`,
      count: data.count,
      revenuePaise: data.revenuePaise
    });
  }

  const ordersCount = parseInt(todayRes.rows[0].order_count, 10);
  const revenuePaise = parseInt(todayRes.rows[0].revenue_paise, 10);
  const activeOutlets = parseInt(outletRes.rows[0].active_outlets, 10);
  const totalOutlets = parseInt(outletRes.rows[0].total_outlets, 10);
  const avgWaitMinutes = Math.max(1, Math.round(parseFloat(waitRes.rows[0].avg_wait_minutes)));

  return {
    kpis: {
      ordersToday: ordersCount,
      revenueTodayPaise: revenuePaise,
      revenueTodayRupees: Math.round(revenuePaise / 100),
      activeOutlets,
      totalOutlets,
      avgWaitMinutes
    },
    ordersByHour: hourlyBreakdown
  };
}

/**
 * Detailed demand breakdown and food waste estimate
 */
export async function getAdminDemand(campusId, dateStr = null) {
  const dateFilter = dateStr || new Date().toISOString().slice(0, 10);

  // Top dishes sold
  const topDishesRes = await query(
    `SELECT oi.name_snapshot as name,
            SUM(oi.qty) as total_qty,
            SUM(oi.unit_price_paise * oi.qty) as total_revenue_paise,
            outl.name as outlet_name
       FROM order_items oi
       JOIN orders o ON o.id = oi.order_id
       JOIN outlets outl ON outl.id = o.outlet_id
      WHERE outl.campus_id = $1
        AND o.status IN ('COLLECTED', 'READY', 'PLACED', 'ACCEPTED', 'PREPARING')
        AND (o.created_at AT TIME ZONE 'Asia/Kolkata')::date = $2::date
      GROUP BY oi.name_snapshot, outl.name
      ORDER BY total_qty DESC
      LIMIT 10`,
    [campusId, dateFilter]
  );

  // Food waste calculation (Mess planned qty vs actual consumed/reserved)
  const wasteRes = await query(
    `SELECT mm.service_date,
            mm.meal,
            mi.name as dish_name,
            COALESCE(mm.planned_qty, 0) as planned_qty,
            COUNT(mr.id) as reserved_qty,
            COALESCE(SUM(pi.qty), 0) as served_qty
       FROM mess_menus mm
       JOIN menu_items mi ON mi.id = mm.menu_item_id
       JOIN outlets outl ON outl.id = mm.outlet_id
  LEFT JOIN mess_reservations mr ON mr.mess_menu_id = mm.id
  LEFT JOIN plate_items pi ON pi.menu_item_id = mm.menu_item_id
                          AND (pi.served_at AT TIME ZONE 'Asia/Kolkata')::date = mm.service_date
      WHERE outl.campus_id = $1
        AND mm.service_date = $2::date
      GROUP BY mm.service_date, mm.meal, mi.name, mm.planned_qty`,
    [campusId, dateFilter]
  );

  const wasteAudit = wasteRes.rows.map((row) => {
    const planned = parseInt(row.planned_qty, 10);
    const consumed = Math.max(parseInt(row.reserved_qty, 10), parseInt(row.served_qty, 10));
    const estimatedSurplus = Math.max(0, planned - consumed);
    const wastePercent = planned > 0 ? Math.round((estimatedSurplus / planned) * 100) : 0;

    return {
      meal: row.meal,
      dishName: row.dish_name,
      plannedQty: planned,
      consumedQty: consumed,
      estimatedSurplus,
      wastePercent,
      status: wastePercent > 20 ? 'HIGH_SURPLUS' : 'BALANCED'
    };
  });

  return {
    date: dateFilter,
    topDishes: topDishesRes.rows.map((r) => ({
      name: r.name,
      outletName: r.outlet_name,
      totalQty: parseInt(r.total_qty, 10),
      totalRevenuePaise: parseInt(r.total_revenue_paise, 10)
    })),
    wasteAudit
  };
}

export default {
  getDemandForecast,
  getAdminOverview,
  getAdminDemand
};
