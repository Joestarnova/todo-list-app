import { TodoList } from "@/components/TodoList";
import { Header } from "@/components/Header";
import { AddTodoForm } from "@/components/AddTodoForm";
import { ClearCompleted } from "@/components/ClearCompleted";

function App() {
  return (
    <div className="min-h-screen bg-bg text-text flex justify-center py-16 px-5">
      <div className="w-full max-w-130">
        <Header />
        <AddTodoForm />
        <TodoList />
        <ClearCompleted />
      </div>
    </div>
  );
}

export default App;
