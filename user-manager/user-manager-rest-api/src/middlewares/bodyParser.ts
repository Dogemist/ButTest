import koaBodyParser from 'koa-bodyparser';
import { Middleware } from '../utils/Router';

export function bodyParser(): Middleware {
  return koaBodyParser();
}
