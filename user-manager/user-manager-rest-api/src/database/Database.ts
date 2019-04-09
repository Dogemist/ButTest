import { DatabaseConfig } from './DatabaseConfig';
import pgPromiseFactory, { IMain, IDatabase } from 'pg-promise';

export type DatabaseConnection = IDatabase<{}>;

export class Database {
  private config: DatabaseConfig;
  private pgPromise: IMain;
  private database: DatabaseConnection;

  constructor(config: DatabaseConfig) {
    this.config = config;
    this.pgPromise = pgPromiseFactory();
    this.database = this.pgPromise(this.config);
  }

  async isConnected(): Promise<boolean> {
    try {
      const db = this.getConnection();
      await db.any('SELECT NOW()');
      return true;
    } catch (error) {
      return false;
    }
  }

  async close(): Promise<void> {
    await this.pgPromise.end();
  }

  getConnection(): DatabaseConnection {
    return this.database;
  }
}
