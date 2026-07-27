import { AppRoutes } from './routes'

// The composition root. It owns the page frame; the route table is in
// `routes.tsx`, and each route reaches its features through their public APIs
// like everyone else — breadth, not depth (CON-005).
export function App() {
  return (
    <main className="max-w-[40rem] mx-auto px-4 pt-6 pb-16">
      <h1 className="text-2xl font-bold mb-4">Budget</h1>
      <AppRoutes />
    </main>
  )
}
