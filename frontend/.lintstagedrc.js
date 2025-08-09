// @ts-nocheck
const path = require("path");

// from: https://nextjs.org/docs/pages/building-your-application/configuring/eslint#lint-staged
const buildEslintCommand = (filenames) =>
  `pnpm lint:target --file ${filenames.map((f) => path.relative(process.cwd(), f)).join(" --file ")}`;

module.exports = {
  // default ESLint extensions, to be in sync with the pipeline command
  "*.{js,mjs,cjs,jsx,ts,mts,cts,tsx}": [buildEslintCommand],
};
