'use client';

import { useEffect, useState } from 'react';
import { useRouter } from 'next/navigation';
import { apiClient, Thought, ThoughtStatus } from '@/lib/api-client';
import { Button } from '@/components/ui/button';
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from '@/components/ui/table';
import {
  AlertDialog,
  AlertDialogAction,
  AlertDialogCancel,
  AlertDialogContent,
  AlertDialogDescription,
  AlertDialogFooter,
  AlertDialogHeader,
  AlertDialogTitle,
} from '@/components/ui/alert-dialog';
import { useToast } from '@/hooks/use-toast';
import { Pencil, Trash2, ThumbsUp, ThumbsDown, ArrowUpDown } from 'lucide-react';

type SortField = 'createdAt' | 'thumbsUp' | 'thumbsDown';
type SortDirection = 'asc' | 'desc';

function calculateRatingPercentage(thought: Thought): {
  percentage: number;
  dominant: 'up' | 'down' | 'none';
} {
  const total = thought.thumbsUp + thought.thumbsDown;

  if (total === 0) {
    return { percentage: 0, dominant: 'none' };
  }

  const maxVotes = Math.max(thought.thumbsUp, thought.thumbsDown);
  const percentage = (maxVotes / total) * 100;
  const dominant = thought.thumbsUp > thought.thumbsDown ? 'up' : 'down';

  return { percentage, dominant };
}

function StatusBadge({ status }: { status: ThoughtStatus }) {
  const colorMap = {
    [ThoughtStatus.APPROVED]: 'bg-green-100 text-green-800',
    [ThoughtStatus.REMOVED]: 'bg-red-100 text-red-800',
    [ThoughtStatus.IN_REVIEW]: 'bg-yellow-100 text-yellow-800',
  };

  return (
    <span className={`px-2 py-1 rounded text-xs font-medium ${colorMap[status]}`}>
      {status}
    </span>
  );
}

export default function ThoughtsListPage() {
  const [thoughts, setThoughts] = useState<Thought[]>([]);
  const [loading, setLoading] = useState(true);
  const [currentPage, setCurrentPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [deleteDialogOpen, setDeleteDialogOpen] = useState(false);
  const [thoughtToDelete, setThoughtToDelete] = useState<Thought | null>(null);
  const [sortField, setSortField] = useState<SortField>('createdAt');
  const [sortDirection, setSortDirection] = useState<SortDirection>('desc');
  const pageSize = 20;
  const router = useRouter();
  const { toast } = useToast();

  useEffect(() => {
    loadThoughts();
  }, [currentPage]);

  useEffect(() => {
    sortThoughts();
  }, [sortField, sortDirection]);

  async function loadThoughts() {
    try {
      setLoading(true);
      const data = await apiClient.getThoughts(currentPage, pageSize);
      setThoughts(data);

      // Calculate total pages based on returned data
      // Since the backend doesn't return pagination metadata, we approximate
      if (data.length < pageSize && currentPage === 0) {
        setTotalPages(1);
      } else if (data.length < pageSize) {
        setTotalPages(currentPage + 1);
      } else {
        setTotalPages(currentPage + 2);
      }
    } catch (error) {
      toast({
        title: 'Error',
        description: 'Failed to load thoughts. Please try again.',
        variant: 'destructive',
      });
    } finally {
      setLoading(false);
    }
  }

  function sortThoughts() {
    const sorted = [...thoughts].sort((a, b) => {
      let comparison = 0;

      if (sortField === 'createdAt') {
        comparison = new Date(a.createdAt).getTime() - new Date(b.createdAt).getTime();
      } else if (sortField === 'thumbsUp') {
        comparison = a.thumbsUp - b.thumbsUp;
      } else if (sortField === 'thumbsDown') {
        comparison = a.thumbsDown - b.thumbsDown;
      }

      return sortDirection === 'asc' ? comparison : -comparison;
    });

    setThoughts(sorted);
  }

  function handleSort(field: SortField) {
    if (sortField === field) {
      setSortDirection(sortDirection === 'asc' ? 'desc' : 'asc');
    } else {
      setSortField(field);
      setSortDirection('desc');
    }
  }

  function handleDeleteClick(thought: Thought) {
    setThoughtToDelete(thought);
    setDeleteDialogOpen(true);
  }

  async function handleDeleteConfirm() {
    if (!thoughtToDelete) return;

    try {
      await apiClient.deleteThought(thoughtToDelete.id);
      setThoughts(thoughts.filter((t) => t.id !== thoughtToDelete.id));
      toast({
        title: 'Success',
        description: 'Thought deleted successfully.',
      });
    } catch (error) {
      toast({
        title: 'Error',
        description: 'Failed to delete thought. Please try again.',
        variant: 'destructive',
      });
    } finally {
      setDeleteDialogOpen(false);
      setThoughtToDelete(null);
    }
  }

  function truncateContent(content: string, maxLength: number = 100): string {
    if (content.length <= maxLength) return content;
    return content.substring(0, maxLength) + '...';
  }

  if (loading) {
    return (
      <div className="container mx-auto py-10">
        <div className="flex justify-between items-center mb-6">
          <h1 className="text-3xl font-bold">Thoughts Management</h1>
          <Button onClick={() => router.push('/thoughts/new')}>Create New Thought</Button>
        </div>
        <div className="rounded-md border">
          <div className="p-8 text-center text-gray-500">Loading thoughts...</div>
        </div>
      </div>
    );
  }

  return (
    <div className="container mx-auto py-10">
      <div className="flex justify-between items-center mb-6">
        <h1 className="text-3xl font-bold">Thoughts Management</h1>
        <Button onClick={() => router.push('/thoughts/new')}>Create New Thought</Button>
      </div>

      <div className="rounded-md border">
        <Table>
          <TableHeader>
            <TableRow>
              <TableHead className="w-[350px]">Content</TableHead>
              <TableHead className="w-[150px]">Author</TableHead>
              <TableHead
                className="cursor-pointer hover:bg-gray-50"
                onClick={() => handleSort('thumbsUp')}
              >
                <div className="flex items-center gap-1">
                  <ThumbsUp className="h-4 w-4" />
                  Up
                  {sortField === 'thumbsUp' && <ArrowUpDown className="h-3 w-3" />}
                </div>
              </TableHead>
              <TableHead
                className="cursor-pointer hover:bg-gray-50"
                onClick={() => handleSort('thumbsDown')}
              >
                <div className="flex items-center gap-1">
                  <ThumbsDown className="h-4 w-4" />
                  Down
                  {sortField === 'thumbsDown' && <ArrowUpDown className="h-3 w-3" />}
                </div>
              </TableHead>
              <TableHead>Rating</TableHead>
              <TableHead>Status</TableHead>
              <TableHead
                className="cursor-pointer hover:bg-gray-50"
                onClick={() => handleSort('createdAt')}
              >
                <div className="flex items-center gap-1">
                  Created
                  {sortField === 'createdAt' && <ArrowUpDown className="h-3 w-3" />}
                </div>
              </TableHead>
              <TableHead className="text-right">Actions</TableHead>
            </TableRow>
          </TableHeader>
          <TableBody>
            {thoughts.length === 0 ? (
              <TableRow>
                <TableCell colSpan={8} className="text-center text-gray-500 py-8">
                  No thoughts found. Create your first thought to get started.
                </TableCell>
              </TableRow>
            ) : (
              thoughts.map((thought) => {
                const rating = calculateRatingPercentage(thought);
                return (
                  <TableRow key={thought.id}>
                    <TableCell className="font-medium">
                      {truncateContent(thought.content)}
                    </TableCell>
                    <TableCell className="text-sm text-gray-600">
                      {truncateContent(thought.author || 'Unknown', 50)}
                    </TableCell>
                    <TableCell>{thought.thumbsUp}</TableCell>
                    <TableCell>{thought.thumbsDown}</TableCell>
                    <TableCell>
                      {rating.dominant === 'none' ? (
                        <span className="text-gray-400 text-sm">No ratings yet</span>
                      ) : (
                        <div className="flex items-center gap-2">
                          {rating.dominant === 'up' ? (
                            <ThumbsUp className="h-4 w-4 text-green-600" />
                          ) : (
                            <ThumbsDown className="h-4 w-4 text-red-600" />
                          )}
                          <span className="font-medium">{rating.percentage.toFixed(1)}%</span>
                        </div>
                      )}
                    </TableCell>
                    <TableCell>
                      <StatusBadge status={thought.status} />
                    </TableCell>
                    <TableCell className="text-sm text-gray-500">
                      {new Date(thought.createdAt).toLocaleDateString()}
                    </TableCell>
                    <TableCell className="text-right">
                      <div className="flex justify-end gap-2">
                        <Button
                          variant="outline"
                          size="sm"
                          onClick={() => router.push(`/thoughts/${thought.id}/edit`)}
                        >
                          <Pencil className="h-4 w-4" />
                        </Button>
                        <Button
                          variant="outline"
                          size="sm"
                          onClick={() => handleDeleteClick(thought)}
                        >
                          <Trash2 className="h-4 w-4 text-red-600" />
                        </Button>
                      </div>
                    </TableCell>
                  </TableRow>
                );
              })
            )}
          </TableBody>
        </Table>
      </div>

      {thoughts.length > 0 && (
        <div className="flex items-center justify-between mt-4">
          <div className="text-sm text-gray-500">
            Page {currentPage + 1} of {totalPages}
          </div>
          <div className="flex gap-2">
            <Button
              variant="outline"
              onClick={() => setCurrentPage(Math.max(0, currentPage - 1))}
              disabled={currentPage === 0}
            >
              Previous
            </Button>
            <Button
              variant="outline"
              onClick={() => setCurrentPage(currentPage + 1)}
              disabled={currentPage >= totalPages - 1 || thoughts.length < pageSize}
            >
              Next
            </Button>
          </div>
        </div>
      )}

      <AlertDialog open={deleteDialogOpen} onOpenChange={setDeleteDialogOpen}>
        <AlertDialogContent>
          <AlertDialogHeader>
            <AlertDialogTitle>Confirm Deletion</AlertDialogTitle>
            <AlertDialogDescription>
              Are you sure you want to delete this thought? This action cannot be undone.
              {thoughtToDelete && (
                <div className="mt-4 p-3 bg-gray-50 rounded text-sm text-gray-700">
                  {truncateContent(thoughtToDelete.content, 150)}
                </div>
              )}
            </AlertDialogDescription>
          </AlertDialogHeader>
          <AlertDialogFooter>
            <AlertDialogCancel>Cancel</AlertDialogCancel>
            <AlertDialogAction onClick={handleDeleteConfirm} className="bg-red-600 hover:bg-red-700">
              Delete
            </AlertDialogAction>
          </AlertDialogFooter>
        </AlertDialogContent>
      </AlertDialog>
    </div>
  );
}

export { ThoughtsListPage };
