import type { TurboModule } from 'react-native';
import { TurboModuleRegistry } from 'react-native';

export interface Spec extends TurboModule {
  nativeRequest(config: Object): Promise<Object>;
  setCookieForDomain(
    domain: string,
    path: string,
    name: string,
    value: string
  ): void;
  removeAllCookiesForDomain(domain: string): void;
  cancelRequestWithId(requestId: string): void;
  cancelAllRunningRequests(): void;
  deallocate(): void;
}

export default TurboModuleRegistry.getEnforcing<Spec>(
  'IoReactNativeHttpClient'
);
