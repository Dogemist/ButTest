import * as os from 'os';

export default class HealthManager {
  metrics() {
    const uptime = os.uptime();
    const platform = os.platform().toString();
    const freeMemory = os.freemem();

    return {
      freeMemory,
      platform,
      uptime,
    };
  }
}
