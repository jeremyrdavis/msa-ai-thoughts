'use client';

import { useState } from 'react';
import { useRouter } from 'next/navigation';
import { useForm } from 'react-hook-form';
import { z } from 'zod';
import { zodResolver } from '@hookform/resolvers/zod';
import { apiClient } from '@/lib/api-client';
import { Button } from '@/components/ui/button';
import {
  Form,
  FormControl,
  FormDescription,
  FormField,
  FormItem,
  FormLabel,
  FormMessage,
} from '@/components/ui/form';
import { Input } from '@/components/ui/input';
import { Textarea } from '@/components/ui/textarea';
import { useToast } from '@/hooks/use-toast';

const thoughtSchema = z.object({
  content: z
    .string()
    .min(10, 'Content must be at least 10 characters')
    .max(500, 'Content must not exceed 500 characters'),
  author: z
    .string()
    .max(200, 'Author must not exceed 200 characters')
    .optional(),
  authorBio: z
    .string()
    .max(200, 'Author Bio must not exceed 200 characters')
    .optional(),
});

type ThoughtFormValues = z.infer<typeof thoughtSchema>;

export default function CreateThoughtPage() {
  const [isSubmitting, setIsSubmitting] = useState(false);
  const router = useRouter();
  const { toast } = useToast();

  const form = useForm<ThoughtFormValues>({
    resolver: zodResolver(thoughtSchema),
    defaultValues: {
      content: '',
      author: '',
      authorBio: '',
    },
  });

  async function onSubmit(values: ThoughtFormValues) {
    try {
      setIsSubmitting(true);
      await apiClient.createThought({
        content: values.content,
        author: values.author,
        authorBio: values.authorBio,
      });
      toast({
        title: 'Success',
        description: 'Thought created successfully.',
      });
      router.push('/thoughts');
    } catch (error: any) {
      toast({
        title: 'Error',
        description: error.message || 'Failed to create thought. Please try again.',
        variant: 'destructive',
      });
    } finally {
      setIsSubmitting(false);
    }
  }

  const contentLength = form.watch('content').length;
  const authorLength = form.watch('author')?.length || 0;
  const authorBioLength = form.watch('authorBio')?.length || 0;

  return (
    <div className="container mx-auto py-10 max-w-2xl">
      <div className="mb-6">
        <h1 className="text-3xl font-bold">Create New Thought</h1>
        <p className="text-gray-500 mt-2">
          Add a new positive thought to the collection. It will be set to IN_REVIEW status by default.
        </p>
      </div>

      <Form {...form}>
        <form onSubmit={form.handleSubmit(onSubmit)} className="space-y-6">
          <FormField
            control={form.control}
            name="content"
            render={({ field }) => (
              <FormItem>
                <FormLabel>Content</FormLabel>
                <FormControl>
                  <Textarea
                    placeholder="Enter a positive thought..."
                    className="min-h-[150px]"
                    {...field}
                  />
                </FormControl>
                <FormDescription>
                  {contentLength} / 500 characters (minimum 10 required)
                </FormDescription>
                <FormMessage />
              </FormItem>
            )}
          />

          <FormField
            control={form.control}
            name="author"
            render={({ field }) => (
              <FormItem>
                <FormLabel>Author</FormLabel>
                <FormControl>
                  <Input
                    placeholder="Enter author name (optional)"
                    {...field}
                  />
                </FormControl>
                <FormDescription>
                  {authorLength} / 200 characters
                </FormDescription>
                <FormMessage />
              </FormItem>
            )}
          />

          <FormField
            control={form.control}
            name="authorBio"
            render={({ field }) => (
              <FormItem>
                <FormLabel>Author Bio</FormLabel>
                <FormControl>
                  <Textarea
                    placeholder="Enter author bio (optional)"
                    className="min-h-[80px]"
                    {...field}
                  />
                </FormControl>
                <FormDescription>
                  {authorBioLength} / 200 characters
                </FormDescription>
                <FormMessage />
              </FormItem>
            )}
          />

          <div className="flex gap-4">
            <Button type="submit" disabled={isSubmitting}>
              {isSubmitting ? 'Creating...' : 'Create Thought'}
            </Button>
            <Button
              type="button"
              variant="outline"
              onClick={() => router.push('/thoughts')}
              disabled={isSubmitting}
            >
              Cancel
            </Button>
          </div>
        </form>
      </Form>
    </div>
  );
}

export { CreateThoughtPage };
