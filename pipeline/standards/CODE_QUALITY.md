# WedNow Code Quality Standards

# Senior Android Engineer Checklist — every rule here is enforced by the Review Agent.

# ============================================================

## 1. Kotlin Style & Correctness

### Force-unwrap (`!!`)

- **BANNED** without a comment explaining why it cannot be null.
- Preferred alternatives: `?: return`, `?: throw IllegalStateException(...)`, `let {}`,
  `requireNotNull()`
- A `!!` that causes a production crash is a P0 bug introduced by the author.

### Nullability

- Never accept platform nullability from Java interop silently. Always annotate or assert.
- Use `@NonNull` / `@Nullable` when calling Java APIs from Kotlin to surface IDE warnings.
- Never use `lateinit var` for a property that could be initialized in the constructor.
- Prefer `lazy {}` over `lateinit var` for expensive properties.

### Immutability

- `val` over `var` everywhere possible.
- Data classes for value objects — never mutable data classes for domain models.
- Expose `List<T>`, not `MutableList<T>`, from any public API.
- Expose `StateFlow`, not `MutableStateFlow`, from ViewModel.
- Expose `Flow`, not `MutableSharedFlow`, from Repository.

### Collections & Sequences

- Use `Sequence` for multi-step transformations on large collections (avoids intermediate lists).
- Prefer `buildList {}`, `buildMap {}` over imperative `add()` patterns.
- Never modify a collection while iterating it — use `filter {}` + `toMutableList()` first.

### String resources

- Zero hardcoded user-visible strings in Kotlin/Compose code.
- All user-facing text in `res/values/strings.xml`.
- Parameterized strings use `stringResource(R.string.x, arg)` — never string concatenation.

---

## 2. Jetpack Compose Performance & Correctness

### Stability & recomposition

- Every data class passed as a Compose parameter must be `@Stable` or `@Immutable` — or be a
  primitive / `String` / `State`.
- `@Stable`: class whose public properties only change in a way that notifies Compose.
- `@Immutable`: class whose public properties never change after construction.
- **Kotlin `data class` is NOT automatically stable.** Annotate explicitly if it contains
  mutable references or non-stable types.
- Run `./gradlew assembleRelease -PcomposeCompilerReports=true` to audit stability.

### Lambda capture — avoid allocation on every recomposition

```kotlin
// BAD — creates a new lambda on every recomposition
Button(onClick = { viewModel.onAction(BookAction.Confirm) })

// GOOD — stable reference
val onConfirm = remember { { viewModel.onAction(BookAction.Confirm) } }
Button(onClick = onConfirm)
```

Or pass `onAction: (BookAction) -> Unit` as a stable lambda down the tree.

### `remember` rules

- Never put business logic inside `remember {}` — it runs on first composition only and
  is NOT re-executed when inputs change (unless you use `remember(key) {}`).
- Use `remember(key1, key2) {}` when the remembered value must be invalidated on input change.
- `rememberSaveable` only for values that must survive process death (simple types or `Saver`).

### `LaunchedEffect` / `SideEffect` / `DisposableEffect`

- `LaunchedEffect(key)` — restarts the coroutine when key changes. Key should be the logical
  trigger, not `Unit` (unless it's truly "run once").
- `DisposableEffect` — use whenever you register a listener/callback; always `onDispose` to
  unregister.
- Never launch a coroutine from inside a composable without `LaunchedEffect` or
  `rememberCoroutineScope`.
- `rememberCoroutineScope()` is for user-triggered one-shots (button tap); never for automatic side
  effects.

### `LazyColumn` / `LazyRow`

- Every item must have a stable, unique `key` parameter. Without it, Compose re-creates
  all item states on list update.
- Never place a `LazyColumn` inside another `LazyColumn` or `NestedScrollView` without
  `nestedScroll` wiring — causes unmeasured content and ANR risk.
- `contentType` parameter for heterogeneous lists enables item reuse.

### Modifier order matters

Always order: size → padding → background → clickable → other.
`clickable` after `padding` means the padding area is not tappable — almost always wrong.

### Avoid unnecessary recomposition

- `derivedStateOf {}` to compute a derived value only when its inputs change (not on every
  recomposition).
- Split large composables into smaller ones so only the affected subtree recomposes.
- Pass only the data a child needs — not the entire UiState object.

---

## 3. Threading & Coroutines

### Dispatcher rules (see also ARCHITECTURE.md)

- **Main thread:** UI updates, `ViewModel` state emissions, `Dispatchers.Main.immediate` for
  synchronous UI-bound work.
- **IO thread:** network, disk, DataStore, Room queries.
- **Default thread:** CPU-intensive work (sorting, parsing, encryption).
- **NEVER** call `withContext(Dispatchers.Main)` inside a ViewModel coroutine to update state —
  `MutableStateFlow.update {}` is thread-safe and can be called from any thread.

### Blocking calls

- Zero `Thread.sleep()`, `BlockingQueue.take()`, or `runBlocking` in production code.
- `runBlocking` is legal only in: `main()` entry points, tests, and top-level `@JvmStatic` helpers.
- Detect blocking main-thread calls with StrictMode in debug builds:
  ```kotlin
  if (BuildConfig.DEBUG) {
      StrictMode.setThreadPolicy(
          StrictMode.ThreadPolicy.Builder().detectAll().penaltyLog().build()
      )
  }
  ```

### Structured concurrency

- `viewModelScope.launch {}` cancels automatically when ViewModel is cleared.
- `lifecycleScope.launch {}` (Activity/Fragment) respects lifecycle.
- For parallel work inside a coroutine, always use `coroutineScope {}` or `supervisorScope {}`.
- `async {}` must always be `.await()`-ed — orphaned Deferred is a coroutine leak.
- Never start a new `Job()` attached to `NonCancellable` outside of cleanup/finalizer logic.

### Flow cold vs hot

- Repository flows are **cold** — they only run when collected.
- ViewModel exposes **hot** `StateFlow` (via `stateIn`). Choose `WhileSubscribed(5_000)` —
  upstream continues during config change (5 s grace) but stops when app is backgrounded.
- Never `collect {}` a cold repository Flow directly in a ViewModel without `stateIn`/`shareIn` —
  each screen recomposition would restart the upstream.

### Cancellation cooperation

- Long-running loops must check `isActive` or call `yield()` at intervals.
- Catch `CancellationException` only to clean up resources — always re-throw it:
  ```kotlin
  catch (e: CancellationException) {
      cleanup()
      throw e  // never swallow
  }
  ```

---

## 4. Memory Management

### Context leaks — most common Android memory leak

- **Never store `Activity`, `Fragment`, or `View` in a `@Singleton`-scoped object.**
- **Never store `Context` in a ViewModel.** Inject `ApplicationContext` via Hilt if needed.
- `@ApplicationContext` Hilt qualifier for app-level context only.
- Lambdas that capture `Activity` and are passed to a longer-lived object (Retrofit callback,
  WorkManager, BroadcastReceiver) must be detached in `onDestroy` / `onStop`.

### Bitmap & image loading

- Never decode bitmaps manually with `BitmapFactory` — use Coil or Glide.
- Coil: `AsyncImage()` in Compose. Always set `size` to the display size, never original.
- Avoid loading full-resolution images into a `LazyColumn` without downsampling.

### Static references

- Zero `companion object { var context: Context? = null }` patterns.
- Static `Handler` or `Runnable` references must be `WeakReference` — or replaced with coroutines.

### ViewHolder / Adapter (if XML is touched)

- Never reference a View after `RecyclerView.Adapter.onViewRecycled()`.
- Always clear image loading requests in `onViewRecycled()`.

### Hilt scope leaks

- A `@Singleton` object that holds a reference to an `@ActivityScoped` object creates a leak.
- `@ViewModelScoped` use cases are scoped to the ViewModel and cleared correctly — but only
  if the ViewModel itself is not stored beyond its scope.

### LeakCanary

- LeakCanary is included in debug builds and its report is read before any PR is approved.
- A PR that introduces a new LeakCanary leak is rejected regardless of functionality.

---

## 5. Security

### Credentials & secrets

- **ZERO** API keys, tokens, passwords, or secrets in source code or string resources.
- Keys live in `local.properties` (git-ignored) → exposed to code via `BuildConfig`.
- Production secrets are injected via CI/CD environment variables into `buildConfigField`.
- `buildConfigField` values are obfuscated by R8 in release builds — still prefer server-side
  token exchange over embedding keys in the APK.

### Data storage

- User tokens, session IDs → `EncryptedDataStore` (Jetpack Security 1.1+).
- PII (name, phone, address) at rest → encrypted Room with `SQLCipher` or column-level encryption.
- Never log PII — `Timber.d("User: $user")` where `user` has a name/email is a security violation.
- `android:allowBackup="false"` in manifest unless backup strategy is explicitly designed.

### Network

- All API traffic over HTTPS. No `cleartext` traffic in production.
  Production `network_security_config.xml` must not have
  `<domain-config cleartextTrafficPermitted="true">`.
- Certificate pinning for all production API endpoints via OkHttp `CertificatePinner`.
- Auth tokens sent in `Authorization: Bearer` header via an OkHttp interceptor — never in URL
  params.
- Token refresh: use a `Authenticator` (OkHttp) to retry 401 responses with a fresh token,
  not ad-hoc retry logic scattered across repositories.

### Input validation

- All user input sanitized before use in Room queries (use parameterized queries — Room does this by
  default).
- Deep link parameters validated and sanitized before processing.
- WebView (if used): `javascript:` URLs disabled, `setJavaScriptEnabled(false)` unless explicitly
  needed,
  `WebViewClient.shouldOverrideUrlLoading()` validates every URL.

### ProGuard / R8

- Release builds always run R8 with full mode enabled.
- Retrofit, Room, Hilt, Gson keep-rules must be present.
- No `-keep class ** { *; }` god rules — keep only what is necessary.

### Exported components

- Every `Activity`, `Service`, `BroadcastReceiver`, `ContentProvider` in the manifest must have
  `android:exported` explicitly set.
- Exported components that handle sensitive actions require `android:permission`.

---

## 6. Error Handling

- Use a `sealed interface` for all UI states: `Loading | Success(data) | Error(UiText)`.
- `UiText` is a sealed class: `StringResource(resId, vararg args)` or `DynamicString(value)`.
  Never pass raw exception messages to the UI.
- Network errors mapped at the Repository level to domain errors (typed sealed class).
- Unexpected exceptions logged via `Timber.e(e)` and surfaced to UI as a generic error message.
- **Never show a stack trace to the user.**
- Retry logic: exponential back-off via `Flow.retryWhen {}` — not recursive calls.

---

## 7. Required Tooling (all must pass in CI)

| Tool                          | Purpose            | Must pass                           |
|-------------------------------|--------------------|-------------------------------------|
| `./gradlew testDebugUnitTest` | Unit tests         | Zero failures                       |
| `./gradlew lintDebug`         | Android Lint       | Zero errors (warnings reviewed)     |
| `ktlint`                      | Kotlin style       | Zero violations                     |
| `detekt`                      | Static analysis    | Zero issues at `error` level        |
| `./gradlew assembleRelease`   | R8 + release build | Successful APK                      |
| LeakCanary report             | Memory leaks       | Zero new leaks                      |
| Compose compiler metrics      | Stability          | No `unstable` classes in public API |

---

## 8. PR Checklist (Review Agent enforces every item)

- [ ] `compileDebugKotlin` — zero errors
- [ ] All unit tests pass
- [ ] Lint — zero new errors
- [ ] No `!!` without an explaining comment
- [ ] No `GlobalScope`
- [ ] No `Thread.sleep()` or `runBlocking` in production code
- [ ] No hardcoded strings, URLs, keys, or secrets
- [ ] No `Activity` / `View` / `Fragment` reference in a ViewModel or Singleton
- [ ] All new public classes and functions have KDoc
- [ ] `MutableStateFlow` is private; only `StateFlow` is exposed
- [ ] Compose parameters that are data classes are annotated `@Stable` or `@Immutable`
- [ ] `LazyColumn` / `LazyRow` items have `key` parameters
- [ ] `LaunchedEffect` keys are correct (not always `Unit` where change matters)
- [ ] `DisposableEffect` has `onDispose` cleanup
- [ ] No new `Log.d/e` calls — Timber only
- [ ] No PII logged
- [ ] New sensitive data stored encrypted
- [ ] No new `cleartext` traffic in network config
- [ ] `android:exported` explicitly set for new manifest components
- [ ] StrictMode does not fire on the new code paths (verify in debug run)
