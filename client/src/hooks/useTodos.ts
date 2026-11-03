import { useMutation, useQuery, useQueryClient  } from "@tanstack/react-query";
import { createTodo, listTodos, updateTodo } from "@/api/todos";
import type { Todo } from "@/types";

export function useTodos() {
  return useQuery({
    queryKey: ["todos"],
    queryFn: listTodos,
  });
}

export function useCreateTodo() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: createTodo,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["todos"] });
    },
  });
}

export function useUpdateTodo() {
    const queryClient = useQueryClient();

    return useMutation({
      mutationFn: (todo: Todo) => updateTodo(todo.id, todo.text, todo.done),
      onSuccess: () => {
        queryClient.invalidateQueries({ queryKey: ["todos"] });
      },
    });
  }