window.addEventListener("message", (event) => {
  if (event.source !== window) return;
  if (event.data?.type !== "ALTERED_HISTORY_TOKEN") return;

  chrome.runtime.sendMessage({
    type: "TOKEN_EXTRACTED",
    token: event.data.token,
  });
});

chrome.runtime.onMessage.addListener((message) => {
  if (message.type !== "ALTERED_HISTORY_LOG") return;
  const method = message.level === "warn" ? "warn" : "info";
  console[method](`[AlteredHistory] ${message.text}`);
});

chrome.runtime.sendMessage({ type: "BGA_PAGE_LOADED" });
