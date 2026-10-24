function redactSensitiveData(data) {
  if (!data || typeof data !== 'object') return data;
  const sensitiveKeys = ['password', 'password_hash', 'token', 'jwt', 'apiKey', 'secret', 'authorization'];
  const sanitized = Array.isArray(data) ? [...data] : { ...data };

  for (const key of Object.keys(sanitized)) {
    if (sensitiveKeys.some((s) => key.toLowerCase().includes(s))) {
      sanitized[key] = '[REDACTED]';
    } else if (typeof sanitized[key] === 'object') {
      sanitized[key] = redactSensitiveData(sanitized[key]);
    }
  }
  return sanitized;
}

export function errorHandler(err, req, res, next) {
  const statusCode = err.status || err.statusCode || 500;
  const errorCode = err.code || (statusCode === 500 ? 'INTERNAL' : 'ERROR');
  const errorMessage = err.message || 'An internal server error occurred';

  // Redact potentially sensitive info from error logging
  const safeReq = {
    method: req.method,
    url: req.originalUrl,
    body: redactSensitiveData(req.body),
    query: redactSensitiveData(req.query)
  };

  if (statusCode >= 500) {
    console.error(`[ERROR 500] ${safeReq.method} ${safeReq.url}:`, err.stack || err);
  } else {
    console.warn(`[WARN ${statusCode}] ${safeReq.method} ${safeReq.url}: ${errorCode} - ${errorMessage}`);
  }

  res.status(statusCode).json({
    error: {
      code: errorCode,
      message: errorMessage
    }
  });
}

export class AppError extends Error {
  constructor(code, message, status = 400) {
    super(message);
    this.code = code;
    this.status = status;
  }
}

export default errorHandler;
