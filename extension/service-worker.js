console.log("AlteredHistory service worker started");

chrome.runtime.onMessage.addListener((message, sender) => {
  if (message.type === "BGA_PAGE_LOADED" && sender.tab?.id) {
    chrome.scripting.executeScript({
      target: { tabId: sender.tab.id },
      world: "MAIN",
      func: () => {
        const token =
          typeof bgaConfig !== "undefined" ? bgaConfig.requestToken : null;
        window.postMessage({ type: "ALTERED_HISTORY_TOKEN", token }, "*");
      },
    });
  }

  if (message.type === "TOKEN_EXTRACTED") {
    if (message.token) {
      chrome.storage.local.set({ requestToken: message.token });
      console.log("AlteredHistory: X-Request-Token captured");
    } else {
      console.warn(
        "AlteredHistory: X-Request-Token not found (user may not be logged in)",
      );
    }
  }
});
