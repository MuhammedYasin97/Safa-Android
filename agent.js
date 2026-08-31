async function askAgent(message) {
  try {
    const response = await fetch("/api/chat", {
      method: "POST",
      headers: {
        "Content-Type": "application/json"
      },
      body: JSON.stringify({
        message: message
      })
    });

    const data = await response.json();

    if (!response.ok) {
      throw new Error(data.error || "حدث خطأ في الخادم");
    }

    if (!data.reply) {
      throw new Error("لم يصل رد من الذكاء الاصطناعي");
    }

    return data.reply;

  } catch (error) {
    console.error("AI Error:", error);

    return "عذرًا، حدث خطأ أثناء الاتصال بالذكاء الاصطناعي.";
  }
}
