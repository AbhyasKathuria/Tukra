export function validate(schema, source = 'body') {
  return (req, res, next) => {
    const dataToValidate = req[source] || {};
    const result = schema.safeParse(dataToValidate);

    if (!result.success) {
      const issues = result.error.issues || result.error.errors || [];
      const issue = issues[0] || { message: 'Validation failed', path: [] };
      const fieldPath = issue.path ? issue.path.join('.') : '';
      const message = fieldPath ? `${fieldPath}: ${issue.message}` : issue.message;
      return res.status(400).json({
        error: {
          code: 'VALIDATION_FAILED',
          message: message || 'Validation failed'
        }
      });
    }

    req[source] = result.data;
    next();
  };
}

export default validate;
