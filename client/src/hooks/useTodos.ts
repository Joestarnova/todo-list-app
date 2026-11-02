import { useMutation, useQuery, useQueryClient  } from "@tanstack/react-query";
import { createTodo, listTodos } from "@/api/todos";

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
