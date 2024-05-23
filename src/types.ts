export type HttpBaseConfig = {
  followRedirects?: boolean;
  headers?: Record<string, string>;
  timeoutMilliseconds?: number;
  url: string;
};
export type HttpGetConfig = HttpBaseConfig & { verb: 'get' };
export type HttpPostConfig = HttpBaseConfig & {
  verb: 'post';
  body?: Record<string, string>;
};
export type HttpCallConfig = HttpGetConfig | HttpPostConfig;

export type HttpClientSuccessResponse = {
  type: 'success';
  status: number;
  body: string;
  headers: Record<string, string | undefined>;
};
export type HttpClientFailureResponse = {
  type: 'failure';
  code: number;
  message: string;
  headers: Record<string, string | undefined>;
};
export type HttpClientResponse =
  | HttpClientSuccessResponse
  | HttpClientFailureResponse;
