import { useTodos } from "@/hooks/useTodos";

export function TodoList() {
  const { data: todos, isPending, isError, error } = useTodos();

  if (isPending) {
    return <p className="py-12 text-center text-sm text-muted">Loading...</p>;
  }

  if (isError) {
    return (
      <p className="py-12 text-center text-sm text-muted">{error.message}</p>
    );
  }

  if (todos.length === 0) {
    return (
      <p className="py-12 text-center text-sm text-muted">
        No tasks yet. Add your first one above.
      </p>
    );
  }

  return (
    <div className="flex flex-col gap-2">
      {todos.map((todo) => (
        <div key={todo.id}>{todo.text}</div>
      ))}
    </div>
  );
}
