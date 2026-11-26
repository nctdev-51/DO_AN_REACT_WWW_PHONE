# PTV Mobile · Apple-inspired UI system

## Context and goals

Present real phones with generous space, clear typography and restrained upward reveals, while keeping shopping, account and administration tasks direct.

This implementation serves **PTV Mobile**, a Vietnamese multi-brand phone retailer. Apple Việt Nam is the visual reference, not the store identity. Repository routes, catalog, checkout and account flows verify the e-commerce context; the supplied extraction's low-confidence audience inference is not used as product research.

The reference extraction reports 274 links, 75 buttons, 56 lists, 2 inputs and 2 navigation regions. These are reference diagnostics, not required counts or targets for PTV pages. Teams should use the smallest navigation and action set that completes the actual task.

## Design tokens and foundations

Runtime sources: `static/css/tokens.css`, `theme.css`, `showcase.css`, `motion.css`; behavior: `static/js/motion.js`.

| Foundation | Token or rule |
| --- | --- |
| Font | `--font-family-stack`: SF Pro Text, Myriad Set Pro, SF Pro Icons, Apple Legacy Chevron, Helvetica Neue, Helvetica, Arial, sans-serif. No remote font dependency. Licensed fonts must only be used where installed/licensed; the stack must remain usable through its fallbacks. |
| Reference base | `--font-size-xs` = 12px; reference line height ≈16px using `--line-height-caption`. This compact size should be reserved for navigation, captions and metadata. |
| Reading text | `--font-size-md` = 17px; `--line-height-body` = 1.5. Forms and descriptions must remain readable without zoom. |
| Type scale | xs 12, sm 14, md 17, lg 19, xl 21, 2xl 25.5, 3xl 28, 4xl 34; shared display extensions 5xl 48, 6xl 64, 7xl 80. Components must select from these tokens rather than add local sizes. |
| Palette primitives | `--color-surface-base`, `--color-surface-strong`, `--color-surface-muted`, `--color-text-secondary`, `--color-text-tertiary`, `--color-text-inverse` preserve the supplied reference palette. |
| Semantic roles | Canvas: `--color-canvas`; card: `--color-card`; foreground: `--color-text`; secondary copy: `--color-text-muted`; links/buttons: `--color-action`; focus: `--color-focus`; feedback: `--color-error`, `--color-success`. Components must use semantic roles, not raw color values. |
| Spacing | `--space-1`…`--space-8` preserve 3, 4, 5, 6, 8, 9, 9.6, 11px. Shared layout extensions `--space-9`…`--space-19` provide 12, 16, 20, 24, 32, 40, 48, 64, 80, 96, 128px. Teams must extend the shared scale before introducing a new spacing value. |
| Radius | `--radius-sm` for pill actions; `--radius-card` for panels/cards; `--radius-field` for inputs; `--radius-small` for compact choices. |
| Shadow | `--shadow-card`, `--shadow-hover`; shadows should support grouping rather than replace boundaries on controls. |
| Motion | instant 100, fast 200, normal 250, slow 320, slower 1000ms. Reveal uses `--motion-duration-slower`, `--motion-distance`, `--motion-ease-reveal` and a maximum of three `--motion-stagger` steps. |
| Layout | `--layout-content-width`, `--layout-copy-width`, `--layout-visual-width`; breakpoints 767/991px. Major layouts must adapt at 320px and through 200% zoom. |

## Component-level rules

### Shared state contract

Every interactive component must implement this contract, with the component-specific behavior below. A state that has no applicable asynchronous operation must remain absent rather than show a fictional loading/error state.

| State | Required behavior |
| --- | --- |
| Default | Text and boundaries must use semantic foreground, surface and control-border roles. The accessible name must describe the action. |
| Hover | Pointer hover must use action-hover/border tokens; information must never require hover. |
| Focus-visible | Keyboard focus must show the shared focus ring, including inside scrolling and dark regions. Focus must never be hidden by motion. |
| Active | Buttons/links must provide pressed styling. Toggles must expose `aria-pressed`; current navigation should expose `aria-current`. |
| Disabled | Native controls must use `disabled`; any `aria-disabled` control must also prevent activation. Disabled styling must remain distinguishable from loading. |
| Loading | Asynchronous regions must expose `aria-busy` and retain their layout. Copy must explain the pending action; a second payment/order submission must not be introduced by visual changes. |
| Error | Errors must use the error role plus meaningful text. Field failures must identify the field; empty search results must offer a recovery action. Color alone must not communicate failure. |

### Header, navigation and footer

- Anatomy: brand link, search form, store links/account menu; grouped footer links. They must use compact type, shared inline spacing and semantic landmarks with distinct names.
- Links must work with Enter and pointer/touch activation. Menus must use native buttons and preserve Bootstrap Escape/outside-click behavior. Mobile navigation must wrap without removing shopping/account destinations.
- Long labels must wrap within their region or use a controlled horizontal scroller. Navigation must not create page-level horizontal overflow. Current-brand selection must remain above the catalog.
- Search must keep an accessible label; its native form submission is the loading fallback and result/error pages must remain readable without JavaScript.

### Showcase, headings and promotional content

- Anatomy: brand caption, one heading, short subtitle, real starting price, two actions and a real product image. The primary showcase must contain the page's single `h1`; subsequent showcases must use `h2`.
- Display headings must use the shared responsive type scale. Vertical spacing must use layout spacing tokens; images must use `object-fit: contain` and fixed responsive image-height tokens to reserve space.
- Images that link to products must have descriptive accessible names and visible focus. Hover scale should be small and must be disabled under reduced motion.
- Missing/unavailable showcase products must fall back to the real catalog action. Copy must not invent releases, discounts, sales, ratings or availability.

### Product cards, filters and sorting

- Anatomy: image, brand, linked name, concise specification, native variant buttons, current price/discount/stock, compare action. Card and chip spacing must use shared tokens.
- Variant controls must be buttons with `aria-pressed`, support Enter/Space/touch, and synchronize image, price, comparison and detail URL. The page must still provide product/compare navigation without JavaScript.
- Filter disclosure must be native `details/summary`; Enter/Space and touch must open it. Existing active filters must be expanded. Sorting must preserve query conditions and show the active order.
- Dense mobile grids must retain readable names/prices and avoid clipping long colors or storage labels. Empty search must provide clear recovery copy. Loaded cards must not shift because their image sizes were omitted.

### Product gallery and 360°

- Photo thumbnails must be real buttons with descriptive names and `aria-pressed`. Previous/next, zoom and close actions must remain keyboard accessible. Native dialog Escape must dismiss zoom and return focus.
- The gallery must announce photo captions, expose `aria-busy` while changing color, and explain missing color photos or failed loading. The previous color must clear immediately; an obsolete response must not replace the latest selection.
- Local spin must use a real ordered sequence, with pointer drag, arrow keys, range control and optional play/stop. Auto-play must never start merely because the region entered the viewport.
- External 3D must load only after choosing 360°, show source attribution, and disclose Internet/color limitations. The 360° action must stay absent when no supported data exists.

### Forms, accounts, checkout and support

- Labels, native inputs, help/error copy and primary actions must use the shared field, type and spacing tokens. Input text must not be made smaller than the reading token merely to reduce density.
- Tab/Shift+Tab must follow DOM order; Enter must submit the appropriate form; native selects must preserve keyboard/touch behavior. Required fields must stay identifiable and must retain server validation/CSRF handling.
- Validation must not remove entered data or expose private account existence through new UI copy. Existing order, payment, email and support states must remain authoritative.
- Empty addresses/orders/support lists must give the next useful action. Long customer text must wrap; quantities, money and stock must remain legible.

### Administration and comparison tables

- Administration must share tokens, focus and feedback while keeping its compact sidebar/table workflow. Destructive actions must preserve existing confirmations.
- Comparison/table headers must retain semantic `th` relationships. Wide data must scroll within the table wrapper, not the document. Keyboard focus on controls inside that wrapper must remain visible.
- Totals, status and differences must remain textually identified. The comparison toggle must preserve its checked state; filtering differences must not hide navigation or purchase actions.
- Empty charts/tables must explain the absence of data instead of inventing sample operational results. Updating data must not replay page entrance animations.

### Scroll reveals

- Eligible headings, product cards and content blocks must rise/fade once when entering the viewport. Nested panels must not receive stacked animations. Sticky viewers/order summaries must not be transformed.
- The implementation must use IntersectionObserver rather than continuous scroll listeners. Completed elements must be unobserved; motion must not change document order or block pointer/keyboard interaction.
- Content must be visible without JavaScript, without IntersectionObserver, after initialization failure, under reduced motion and in print. Changing to reduced motion during an animation must reveal everything immediately.
- Keyboard focus must reveal its containing block immediately. Hash targets and restored browser-history pages must not remain transparent. The reveal must never use `aria-hidden` to hide real content from assistive technology.
- Pointer focus must not finish a running transform between pointer-down and pointer-up; otherwise the action may move away before a click completes. The real add-to-cart flow must be exercised while entrance motion is enabled.

## Accessibility requirements and testable acceptance criteria

The target is WCAG 2.2 AA. Automated checks support this target; they are not a claim of complete conformance.

| Pass/fail check | Acceptance criterion |
| --- | --- |
| Contrast | Normal text must reach 4.5:1; large text must reach 3:1. Active control boundaries/focus indicators must reach 3:1 against adjacent colors. Check computed colors in light, dark, hover and error states. |
| Keyboard | A complete product → variant → compare/cart → account/checkout flow must work using Tab, Shift+Tab, Enter, Space, arrows and Escape without a trap or an invisible focused control. |
| Motion | Emulating `prefers-reduced-motion: reduce` must leave no pending/transparent blocks. Switching the preference at runtime must also reveal every pending block. |
| Progressive enhancement | With JavaScript disabled or IntersectionObserver removed, headings, products, prices, navigation and native forms must remain visible and usable. |
| Reflow | At 320, 390, 768 and 1440px, document width must not exceed viewport width. At 200% zoom, content/actions must remain available; wide tables may scroll internally. |
| Targets | Interactive targets must meet the 24×24 CSS-pixel AA minimum or its spacing exception. Main actions should provide 44–48px touch height. |
| Labels and status | Inputs/buttons must have meaningful accessible names; selected variants must expose pressed state. Gallery loading/captions and field errors must be available to assistive technology. |
| Semantics | Each storefront page must have one main region and meaningful heading order. Menus must not replace links with clickable noninteractive containers. |
| Zoom and overlays | Gallery dialog must close with Escape and return focus. Sticky headers must not cover the focused field or in-page destination. |

## Content and tone standards

Copy should be short, factual and useful. Actions must identify their destination or operation.

| Prefer | Avoid |
| --- | --- |
| “Khám phá sản phẩm”, “So sánh điện thoại”, “Xem đơn hàng” | Unqualified “Xem thêm” on unrelated controls |
| “Chưa có ảnh riêng cho màu này” | Showing another color as if it were the selected color |
| Real price, availability and verified reviews | Invented urgency, sales counts, ratings or affiliations |
| “Thử thay đổi từ khóa hoặc khoảng giá” | Empty results with no recovery action |

## Anti-patterns, migration notes and edge cases

- Teams must not copy Apple's identity, legal claims, unpublished product data or licensed font files into PTV Mobile.
- New components must not add raw-color or one-off spacing/type exceptions. Teams should add shared semantic tokens when a new role is justified.
- Teams must not make all body copy 12px based on a compact-navigation extraction. The supplied palette's black primitive must not automatically become every page's canvas.
- Teams must not hide page content in base CSS awaiting JavaScript, hijack scrolling, auto-start 360° rotation, remove focus outlines, or attach reveals to live checkout/stock updates.
- Existing business markup/CSRF/URLs remain the source of behavior. `theme.css` adapts existing layouts; `tokens.css` provides compatibility aliases so older pages inherit the same semantic roles.
- Long product names, no stock, missing hero products, one/zero variants, missing images, slow/aborted gallery responses, table overflow, no orders and no chart data must retain explicit recovery states.

Reference: [Apple Việt Nam](https://www.apple.com/vn/), [IntersectionObserver](https://developer.mozilla.org/en-US/docs/Web/API/Intersection_Observer_API), [reduced-motion media query](https://developer.mozilla.org/en-US/docs/Web/CSS/@media/prefers-reduced-motion).

## QA checklist

- [ ] Shared tokens and all applicable states reviewed on storefront, account/auth and admin routes.
- [ ] Desktop/mobile screenshots checked after scrolling reveals into view.
- [ ] Actual upward movement, bounded stagger and one-time playback verified.
- [ ] Reduced motion, live preference change, no JavaScript, no observer and print verified.
- [ ] Keyboard focus, variants, compare, gallery dialog, forms and sorting verified.
- [ ] Reflow and long labels checked at 320/390/768/1440px and 200% zoom.
- [ ] Computed contrast checked for copy, actions, controls and focus.
- [ ] No broken images, JavaScript errors or document overflow.
- [ ] Existing application tests and WAR packaging pass.



## Store journey components

All new components must inherit semantic tokens from `tokens.css`; layout rules live in `retail.css`, behavior in `retail.js`. Content must remain escaped plain text. Product names and links must replace internal SKU lists in customer buying flows.

| Component | Anatomy / responsive handling | States and interactions |
| --- | --- | --- |
| Search suggestions | Labeled combobox, listbox with up to six product options, image/name/price. Panel must fit viewport and scroll internally. | Default: closed. Hover/active: highlighted option. Focus-visible must remain on input. Up/Down must select options, Enter must follow selected option or submit search, Escape/Tab/outside pointer must close. Short/empty query must stay closed; empty/error/network failure must preserve normal search submission. Loading must not erase typed text. Disabled follows native input state. Touch must follow an option link. |
| Applied filters | Wrap chip links below controls. Each chip must remove its own parameter and reset pagination while keeping other parameters. | Hover/focus/active must inherit link states; native navigation supplies loading and error recovery. Inactive filters must not render disabled decorative chips. Long labels must wrap without widening the document. |
| Product rail / collections | Native horizontal overflow with scroll snap, product cards and a link to all results. Small screens must expose another card edge. | Keyboard focus and native touch/pointer scrolling must work without scroll hijacking. Cards must retain existing default/hover/focus/active/disabled/out-of-stock rules. Empty collections must not invent products; query failure must leave navigation available. |
| Sticky purchase | Current product, selected configuration, price and buy button bound to the original purchase form. Mobile must wrap product name above price/action. | Default/hover/focus/active must use primary button tokens. Out-of-stock must disable buy; selection must update price and label. Submission must preserve the clicked button name/value. Errors must return usable form and selected configuration. Bar must not cover focused content; document bottom/scroll padding must account for its height. |
| Coupons | Code, description, numeric discount, conditions, date, optional qualifying product links, copy/apply actions and live status. | Copy must report success or a manual-copy fallback. Exhausted code must disable apply. Server must recheck scope, dates, balance and quota. Pointer/touch/keyboard must use native buttons/links; expired/empty results must explain recovery. Cards must stack on narrow viewports. |
| Points / consultation / refund forms | Labeled fields, field constraints, summary/history and explicit result state. Two-column forms must become one column on mobile. | Native hover/focus/active states must remain visible. Required/invalid fields must block invalid submission and expose server errors. Pending POST must expose busy state and prevent repeated submission while keeping submitted values. Disabled/closed requests must show readable status and response. Empty histories must explain when entries appear. Success and error navigation must restore controls; browser history restoration must clear busy state. |

QA must include actual matched-variant ordering, no-JS cards/filter links, keyboard combobox, selected-variant buy-now, insufficient points, duplicate delivery/cancel/refund, rejected scope, request ownership, return inspection and 320px reflow. Automated checks must not label dormant hidden gallery images without `src` as failed network image loads.

## Address, campaign and carrier additions

- Address pickers must use native labeled selects with a dependent ward list. Default is an empty province; hover/focus-visible/active must inherit control tokens. Ward must be disabled until a province is selected. Loading must set `aria-busy`, retain the selected province and announce status. Clearing province or aborting a stale request must clear busy state. Error must expose typed-name fallback and a retry action; a hidden focused selector must transfer focus to the fallback. Without JavaScript, typed names must remain usable and be validated server-side against the same directory. Long labels must wrap inside the page; current province and ward codes must survive form errors and saved-address changes.
- Campaign banners must use shared light/dark surface/text tokens, preserve image space during loading and use a descriptive action label. Pointer/touch/keyboard activation must follow the same internal link. Hover/focus-visible/active must use existing action styles. An inactive, expired or unavailable-product banner must not appear as a disabled fake promotion; the homepage fallback must remain available when no campaign is eligible. Admin save/upload must show pending state, retain entered fields on error and reject invalid date ranges/stale versions. An uploaded image must have a meaningful title or alt fallback; no arbitrary external destination or HTML may be rendered.
- Carrier actions must show the configured staging/production environment before dispatch. Default/hover/focus-visible/active must inherit button states. Unconfigured integration must show readable guidance and keep manual shipping available. Pending submit must prevent repeated clicks; the server must also keep the request reference/payload stable on retry. Error must distinguish an unconfirmed dispatch from a confirmed tracking result, expose no token/provider response body, and preserve the ability to reconcile. Status from the carrier must be labeled separately from the shop's final delivery/payment confirmation. Native forms must work with keyboard, pointer and touch; narrow layouts must stack parcel fields.
