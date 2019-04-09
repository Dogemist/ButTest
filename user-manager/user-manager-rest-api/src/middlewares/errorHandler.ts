import { INTERNAL_SERVER_ERROR } from 'http-status-codes';
import { Middleware } from '../utils/Router';
import { Logger } from '../config/logger';
import { AppError } from '../errors/AppError';
import { getHTTPStatusFromErrorCode } from '../errors/errorCodeMap';

export function errorHandler(logger: Logger): Middleware {
  return async (ctx, next) => {
    try {
      await next();
    } catch (err) {
      logger.error(`Error handler triggered: ${err}`);

      if (err instanceof AppError) {
        ctx.body = err.toJSON();
        ctx.status = getHTTPStatusFromErrorCode(err.errorKey);
      } else {
        ctx.body = {
          error: true,
          message: err,
        };
        ctx.status = INTERNAL_SERVER_ERROR;
      }
    }
  };
}
