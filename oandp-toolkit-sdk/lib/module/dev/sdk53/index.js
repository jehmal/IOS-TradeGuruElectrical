"use strict";

import { OandPErrorBoundary } from "../error-boundary.js";
import { OandPSafeInsets } from "../safe-insets-content";
import { BundleInspector } from "./inspector";
import { jsx as _jsx } from "react/jsx-runtime";
export function OandPDevWrapper({
  children
}) {
  return /*#__PURE__*/_jsx(OandPErrorBoundary, {
    children: /*#__PURE__*/_jsx(BundleInspector, {
      children: /*#__PURE__*/_jsx(OandPSafeInsets, {
        children: children
      })
    })
  });
}
//# sourceMappingURL=index.js.map
