import jwt from 'jsonwebtoken';
import config from '../config.js';

export function auth(allowedRoles = []) {
  return (req, res, next) => {
    const authHeader = req.headers.authorization;
    if (!authHeader || !authHeader.startsWith('Bearer ')) {
      return res.status(401).json({
        error: {
          code: 'UNAUTHENTICATED',
          message: 'Missing or malformed Authorization header'
        }
      });
    }

    const token = authHeader.split(' ')[1];
    try {
      const decoded = jwt.verify(token, config.JWT_SECRET);
      req.user = decoded; // { id, role, campusId, outletId? }

      if (allowedRoles.length > 0 && !allowedRoles.includes(decoded.role)) {
        return res.status(403).json({
          error: {
            code: 'FORBIDDEN',
            message: `Role ${decoded.role} is not permitted to access this resource`
          }
        });
      }

      next();
    } catch (err) {
      return res.status(401).json({
        error: {
          code: 'UNAUTHENTICATED',
          message: 'Invalid or expired token'
        }
      });
    }
  };
}

export default auth;
