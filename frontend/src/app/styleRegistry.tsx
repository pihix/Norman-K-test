"use client";
import { useState } from "react";

import createCache from "@emotion/cache";
import { CacheProvider } from "@emotion/react";
import { useServerInsertedHTML } from "next/navigation";

/**
 * Register emotion styles client side using a Client-side component
 * @see https://nextjs.org/docs/app/building-your-application/styling/css-in-js#configuring-css-in-js-in-app
 * @see https://github.com/emotion-js/emotion/issues/2928
 */
export const RootStyleRegistry: React.FC<React.PropsWithChildren> = ({ children }) => {
  const [emotionCache] = useState(() => {
    const cache = createCache({ key: "css" });
    cache.compat = true;
    return cache;
  });

  useServerInsertedHTML(() => {
    return (
      <style
        key="emotion-css"
        data-emotion={`${emotionCache.key} ${Object.keys(emotionCache.inserted).join(" ")}`}
        dangerouslySetInnerHTML={{
          __html: Object.values(emotionCache.inserted).join(" "),
        }}
      />
    );
  });

  return <CacheProvider value={emotionCache}>{children}</CacheProvider>;
};
