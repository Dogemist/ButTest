import { ConfigurationError } from './errors/ConfigurationError';
import { ConfigurationCastError } from './errors/ConfigurationCastError';

type NonNullable<T> = T extends (null | undefined) ? never : T;

export default class ConfigManager {
  private ConfigManager() {
    // empty constructor to prevent subclassing
  }

  static getStringProperty(property: string, defaultProperty?: string) {
    return ConfigManager.getEnv(property, defaultProperty, ConfigManager.identity);
  }

  static getBooleanProperty(property: string, defaultProperty?: boolean) {
    return ConfigManager.getEnv(property, defaultProperty, ConfigManager.stringToBooleanMapper);
  }

  static getIntProperty(property: string, defaultProperty?: number) {
    return ConfigManager.getEnv(property, defaultProperty, ConfigManager.stringToIntegerMapper);
  }

  private static getOptionalEnv(property: string) {
    return process.env[property];
  }

  private static getEnv<T>(
    property: string,
    defaultProperty: T,
    mapper: (value: string) => NonNullable<T>,
  ): NonNullable<T> {
    const envProperty = ConfigManager.getOptionalEnv(property);
    if (envProperty != null) {
      return mapper(envProperty);
    } else {
      if (defaultProperty) {
        return defaultProperty!;
      } else {
        throw new ConfigurationError(property);
      }
    }
  }

  private static identity(k: string): string {
    return k;
  }

  private static stringToBooleanMapper(str: string): boolean {
    if (str.toLowerCase() === 'true') {
      return true;
    } else {
      return false;
    }
  }

  private static stringToIntegerMapper(str: string): number {
    try {
      return Number.parseInt(str, 10);
    } catch (err) {
      throw new ConfigurationCastError(str, 'integer');
    }
  }
}
