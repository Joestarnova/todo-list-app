import { TodoList } from "@/components/TodoList";
import { Header } from "@/components/Header";

function App() {
  return (
    <div className="min-h-screen bg-bg text-text flex justify-center py-16 px-5">
      <div className="w-full max-w-130">
        <Header />
        <TodoList />
      </div>
    </div>
  );
}

export default App;
