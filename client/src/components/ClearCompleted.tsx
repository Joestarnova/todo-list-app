import { useClearCompleted, useTodos } from "@/hooks/useTodos";

  export function ClearCompleted() {
    const { data: todos } = useTodos();
    const clear = useClearCompleted();

    const hasDone = todos?.some((todo) => todo.done);
    if (!hasDone) return null;

    return (
      <div className="mt-5 flex justify-end">
        <button
          type="button"
          onClick={() => clear.mutate()}
          disabled={clear.isPending}
          className="cursor-pointer p-1.5 text-[13px] text-muted"
        >
          Clear completed
        </button>
      </div>
    );
  }