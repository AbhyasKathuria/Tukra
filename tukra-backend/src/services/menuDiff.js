/**
 * Normalizes item names for reliable comparison:
 * - lowercase
 * - strip punctuation and special characters
 * - collapse multiple spaces
 */
export function normalizeName(name) {
  if (!name || typeof name !== 'string') return '';
  return name
    .toLowerCase()
    .replace(/[^\w\s]/gi, '')
    .replace(/\s+/g, ' ')
    .trim();
}

/**
 * Diffs scraped menu items from the web agent against current database records.
 *
 * @param {Array} scrapedItems - Extracted items: [{ name, priceRupees, pricePaise, category, isVeg, soldOut }]
 * @param {Array} currentMenuItems - Current DB items: [{ id, name, price_paise, is_veg, is_available }]
 * @returns {Array} List of proposal objects
 */
export function diffMenu(scrapedItems = [], currentMenuItems = []) {
  const proposals = [];

  // Map existing items by normalized name
  const existingMap = new Map();
  for (const item of currentMenuItems) {
    const norm = normalizeName(item.name);
    existingMap.set(norm, item);
  }

  const seenInScrape = new Set();

  for (const scraped of scrapedItems) {
    if (!scraped || !scraped.name) continue;

    const norm = normalizeName(scraped.name);
    seenInScrape.add(norm);

    // Calculate price in paise
    let newPricePaise = scraped.pricePaise;
    if (newPricePaise === undefined && typeof scraped.priceRupees === 'number') {
      newPricePaise = Math.round(scraped.priceRupees * 100);
    }
    if (newPricePaise === undefined && typeof scraped.price === 'number') {
      newPricePaise = Math.round(scraped.price * 100);
    }

    const isSoldOut = Boolean(scraped.soldOut || scraped.sold_out);
    const existing = existingMap.get(norm);

    if (!existing) {
      // 1. NEW item found on webpage
      proposals.push({
        changeType: 'NEW',
        menuItemId: null,
        itemName: scraped.name.trim(),
        oldPricePaise: null,
        newPricePaise: newPricePaise !== undefined ? newPricePaise : 0,
        flagged: false,
        isVeg: scraped.isVeg !== undefined ? scraped.isVeg : true,
        category: scraped.category || 'Mains'
      });
    } else {
      // 2. PRICE_CHANGE
      if (newPricePaise !== undefined && newPricePaise !== existing.price_paise) {
        const ratio = existing.price_paise > 0 ? newPricePaise / existing.price_paise : 1;
        // Sanity check: Flag price changes that are > 3x or < 0.33x
        const flagged = ratio > 3.0 || ratio < 0.33;

        proposals.push({
          changeType: 'PRICE_CHANGE',
          menuItemId: existing.id,
          itemName: existing.name,
          oldPricePaise: existing.price_paise,
          newPricePaise,
          flagged
        });
      }

      // 3. AVAILABILITY change
      // If page marks sold out, but DB has is_available=true (or vice-versa)
      if (isSoldOut === existing.is_available) {
        proposals.push({
          changeType: 'AVAILABILITY',
          menuItemId: existing.id,
          itemName: existing.name,
          oldPricePaise: existing.price_paise,
          newPricePaise: existing.price_paise,
          flagged: false,
          isAvailable: !isSoldOut
        });
      }
    }
  }

  // 4. REMOVED: In DB but completely missing from the scraped page
  for (const item of currentMenuItems) {
    const norm = normalizeName(item.name);
    if (!seenInScrape.has(norm) && item.is_available) {
      proposals.push({
        changeType: 'REMOVED',
        menuItemId: item.id,
        itemName: item.name,
        oldPricePaise: item.price_paise,
        newPricePaise: null,
        flagged: false
      });
    }
  }

  return proposals;
}

export default {
  normalizeName,
  diffMenu
};
