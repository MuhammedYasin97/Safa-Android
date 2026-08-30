const form = document.getElementById("chatForm");
const input = document.getElementById("messageInput");
const chat = document.getElementById("chat");

function addMessage(text, type) {
  const el = document.createElement("div");
  el.className = `message ${type}`;
  el.textContent = text;
  chat.appendChild(el);
  chat.scrollTop = chat.scrollHeight;
}

form.addEventListener("submit", async (event) => {
  event.preventDefault();
  const message = input.value.trim();
  if (!message) return;

  addMessage(message, "user");
  input.value = "";
  input.disabled = true;

  try {
    const reply = await askAgent(message);
    addMessage(reply, "assistant");
  } catch (error) {
    addMessage("حدث خطأ أثناء تنفيذ الطلب.", "assistant");
    console.error(error);
  } finally {
    input.disabled = false;
    input.focus();
  }
});
