import { Middleware } from 'koa';
import KoaRouter, { IRouterParamContext } from 'koa-router';

export type Middleware = Middleware;

export type RouteMiddleware<StateT, CustomT> =
  Middleware<StateT, CustomT & IRouterParamContext<StateT, CustomT>>;

export interface RouterMiddleware<StateT = any, CustomT = {}> {
  get: (path: string, ...middleware: Middleware[]) => RouterMiddleware<StateT, CustomT>;
  post: (path: string, ...middleware: Middleware[]) => RouterMiddleware<StateT, CustomT>;
  put: (path: string, ...middleware: Middleware[]) => RouterMiddleware<StateT, CustomT>;
  patch: (path: string, ...middleware: Middleware[]) => RouterMiddleware<StateT, CustomT>;
  delete: (path: string, ...middleware: Middleware[]) => RouterMiddleware<StateT, CustomT>;
  routes: () => RouteMiddleware<StateT, CustomT>;
  allowedMethods: () => RouteMiddleware<StateT, CustomT>;
}

export enum HTTPVerb {
  get = 'get',
  post = 'post',
  put = 'put',
  patch = 'patch',
  delete = 'delete',
}

export class Router<StateT = any, CustomT = {}> implements RouterMiddleware<StateT, CustomT> {
  private router: KoaRouter;

  constructor(prefix?: string) {
    this.router = new KoaRouter<StateT, CustomT>({ prefix });
  }

  get(path: string, ...middleware: Middleware[]) {
    return this.on(HTTPVerb.get, path, ...middleware);
  }

  post(path: string, ...middleware: Middleware[]) {
    return this.on(HTTPVerb.post, path, ...middleware);
  }

  put(path: string, ...middleware: Middleware[]) {
    return this.on(HTTPVerb.put, path, ...middleware);
  }

  patch(path: string, ...middleware: Middleware[]) {
    return this.on(HTTPVerb.patch, path, ...middleware);
  }

  delete(path: string, ...middleware: Middleware[]) {
    return this.on(HTTPVerb.delete, path, ...middleware);
  }

  routes(): RouteMiddleware<StateT, CustomT> {
    return this.router.routes();
  }

  allowedMethods(): RouteMiddleware<StateT, CustomT> {
    return this.router.allowedMethods();
  }

  private on(verb: HTTPVerb, path: string, ...middleware: Middleware[]) {
    this.router[verb](path, ...middleware);
    return this;
  }
}
