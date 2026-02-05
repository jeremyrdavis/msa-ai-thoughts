"use client";

import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Textarea } from "@/components/ui/textarea";
import { Label } from "@/components/ui/label";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from "@/components/ui/table";
import { useToast } from "@/hooks/use-toast";

export default function TestComponentsPage() {
  const { toast } = useToast();

  return (
    <div className="min-h-screen p-8 space-y-8">
      <h1 className="text-3xl font-bold mb-4">Component Test Page</h1>

      <section className="space-y-4">
        <h2 className="text-2xl font-semibold">Buttons</h2>
        <div className="flex gap-4">
          <Button variant="default">Default Button</Button>
          <Button variant="secondary">Secondary Button</Button>
          <Button variant="outline">Outline Button</Button>
          <Button variant="destructive">Destructive Button</Button>
          <Button variant="ghost">Ghost Button</Button>
        </div>
      </section>

      <section className="space-y-4">
        <h2 className="text-2xl font-semibold">Form Inputs</h2>
        <div className="max-w-md space-y-4">
          <div>
            <Label htmlFor="test-input">Input Label</Label>
            <Input id="test-input" placeholder="Enter text here" />
          </div>
          <div>
            <Label htmlFor="test-textarea">Textarea Label</Label>
            <Textarea id="test-textarea" placeholder="Enter longer text here" />
          </div>
          <div>
            <Label htmlFor="test-select">Select Label</Label>
            <Select>
              <SelectTrigger id="test-select">
                <SelectValue placeholder="Select an option" />
              </SelectTrigger>
              <SelectContent>
                <SelectItem value="option1">Option 1</SelectItem>
                <SelectItem value="option2">Option 2</SelectItem>
                <SelectItem value="option3">Option 3</SelectItem>
              </SelectContent>
            </Select>
          </div>
        </div>
      </section>

      <section className="space-y-4">
        <h2 className="text-2xl font-semibold">Table</h2>
        <Table>
          <TableHeader>
            <TableRow>
              <TableHead>Name</TableHead>
              <TableHead>Status</TableHead>
              <TableHead>Count</TableHead>
            </TableRow>
          </TableHeader>
          <TableBody>
            <TableRow>
              <TableCell>Item 1</TableCell>
              <TableCell>Active</TableCell>
              <TableCell>42</TableCell>
            </TableRow>
            <TableRow>
              <TableCell>Item 2</TableCell>
              <TableCell>Inactive</TableCell>
              <TableCell>17</TableCell>
            </TableRow>
          </TableBody>
        </Table>
      </section>

      <section className="space-y-4">
        <h2 className="text-2xl font-semibold">Toast Notifications</h2>
        <div className="flex gap-4">
          <Button
            onClick={() => {
              toast({
                title: "Success",
                description: "This is a success message",
              });
            }}
          >
            Show Success Toast
          </Button>
          <Button
            variant="destructive"
            onClick={() => {
              toast({
                variant: "destructive",
                title: "Error",
                description: "This is an error message",
              });
            }}
          >
            Show Error Toast
          </Button>
        </div>
      </section>

      <section className="space-y-4">
        <h2 className="text-2xl font-semibold">Tailwind CSS Test</h2>
        <div className="grid grid-cols-3 gap-4">
          <div className="p-4 bg-primary text-primary-foreground rounded-md">
            Primary Background
          </div>
          <div className="p-4 bg-secondary text-secondary-foreground rounded-md">
            Secondary Background
          </div>
          <div className="p-4 bg-muted text-muted-foreground rounded-md">
            Muted Background
          </div>
        </div>
      </section>
    </div>
  );
}
