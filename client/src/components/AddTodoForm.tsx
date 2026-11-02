import { useState } from "react";
import { useCreateTodo } from "@/hooks/useTodos";

export function AddTodoForm() {
  const [draft, setDraft] = useState("");
  const addTodo = useCreateTodo();

  return (
    <form
      onSubmit={(e) => {
        e.preventDefault();
        const text = draft.trim();
        if (!text) return;
        addTodo.mutate(text, {
          onSuccess: () => setDraft(""),
        });
      }}
      className="mb-7 flex gap-2"
    >
      <input
        value={draft}
        onChange={(e) => setDraft(e.target.value)}
        placeholder="Add a task and press Enter"
        className="flex-1 rounded-[10px] border border-border bg-surface px-3.5 py-3 text-[15px] outline-none placeholder:text-muted"
      />
      <button
        type="submit"
        disabled={addTodo.isPending}
        className="cursor-pointer rounded-[10px] bg-accent px-4.5 text-sm font-medium text-white disabled:opacity-50"
      >
        Add
      </button>
    </form>
  );
}
