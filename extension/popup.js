document.addEventListener("DOMContentLoaded", async () => {
  try {
    const [tab] = await chrome.tabs.query({
      active: true,
      currentWindow: true,
    });

    const url = new URL(tab.url);
    const playerId = url.searchParams.get("id");
    const isBgaHistoryPage =
      url.hostname === "boardgamearena.com" &&
      url.pathname === "/player" &&
      playerId &&
      url.searchParams.get("section") === "lastresults";

    if (!isBgaHistoryPage) {
      showDisconnected();
      return;
    }

    const cookie = await chrome.cookies.get({
      url: "https://boardgamearena.com",
      name: "PHPSESSID",
    });

    if (!cookie) {
      showDisconnected();
      return;
    }

    const results = await chrome.scripting.executeScript({
      target: { tabId: tab.id },
      func: () => {
        const el = document.getElementById("real_player_name");
        return el ? el.textContent.trim() : null;
      },
    });
    const playerName = results?.[0]?.result;

    if (!playerName) {
      showDisconnected();
      return;
    }

    await chrome.storage.local.set({ playerId, playerName });
    showConnected(playerName);
  } catch (err) {
    console.warn("AlteredHistory: session check failed", err);
    showDisconnected();
  }
});

function showConnected(name) {
  document.getElementById("status-loading").classList.add("hidden");
  document.getElementById("status-disconnected").classList.add("hidden");
  document.getElementById("status-connected").classList.remove("hidden");
  document.getElementById("player-name").textContent = name;
}

function showDisconnected() {
  document.getElementById("status-loading").classList.add("hidden");
  document.getElementById("status-connected").classList.add("hidden");
  document.getElementById("status-disconnected").classList.remove("hidden");
}
