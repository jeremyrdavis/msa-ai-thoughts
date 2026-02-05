"use client";

import { useRouter } from 'next/navigation';
import { Button } from '@/components/ui/button';

export default function Home() {
  const router = useRouter();

  return (
    <div className="min-h-screen p-8">
      <div className="max-w-4xl mx-auto">
        <h1 className="text-3xl font-bold mb-4">MSA AI Admin</h1>
        <p className="text-gray-600 mb-8">Admin interface for managing thoughts</p>

        <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-3">
          <div className="p-6 border rounded-lg">
            <h2 className="text-xl font-semibold mb-2">Thoughts Management</h2>
            <p className="text-sm text-gray-600 mb-4">
              Create, edit, and manage positive thoughts with status control.
            </p>
            <Button onClick={() => router.push('/thoughts')}>
              Manage Thoughts
            </Button>
          </div>
        </div>
      </div>
    </div>
  );
}
