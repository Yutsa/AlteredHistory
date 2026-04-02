window.addEventListener("message", (event) => {
  if (event.source !== window) return;
  if (event.data?.type !== "ALTERED_HISTORY_TOKEN") return;

  chrome.runtime.sendMessage({
    type: "TOKEN_EXTRACTED",
    token: event.data.token,
  });
});

chrome.runtime.sendMessage({ type: "BGA_PAGE_LOADED" });
