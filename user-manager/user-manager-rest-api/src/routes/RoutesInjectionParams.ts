import { Database } from '../database/Database';
import { Logger } from '../config/logger';

export interface RoutesInjectionParams {
  database: Database;
  logger: Logger;
}
