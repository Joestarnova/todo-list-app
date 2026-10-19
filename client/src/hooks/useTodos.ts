import { useQuery } from "@tanstack/react-query";
import { listTodos } from "@/api/todos";

export function useTodos() {
  return useQuery({
    queryKey: ["todos"],
    queryFn: listTodos
  })
}