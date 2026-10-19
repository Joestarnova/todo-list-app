import { TodoList } from "@/components/TodoList";

function App() {
  return (
    <div className="min-h-screen bg-bg text-text flex justify-center py-16 px-5">
      <div className="w-full max-w-130">
        <header className="mb-8">
          <h1 className="mb-1.5 text-[28px] font-semibold tracking-[-0.02em]">
            Tasks
          </h1>
          <p className="text-sm text-muted">1 of 2 remaining</p>
        </header>
        <TodoList />
      </div>
    </div>
  );
}

export default App;
