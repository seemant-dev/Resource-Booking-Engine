import Link from "next/link";
import type { Resource } from "@/types";
import { Badge, Table, TableCell, TableHeaderCell } from "@/components/ui";

interface AdminResourcesTableProps {
  resources: Resource[];
}

/**
 * Screen 09 — admin Resources list table. "Manage" opens the resource
 * detail screen, the admin equivalent of the discovery step on the user
 * side, matching the approved design's table-with-action-pill pattern.
 */
export function AdminResourcesTable({ resources }: AdminResourcesTableProps) {
  return (
    <Table>
      <thead>
        <tr>
          <TableHeaderCell>Name</TableHeaderCell>
          <TableHeaderCell>Type</TableHeaderCell>
          <TableHeaderCell>Active slots</TableHeaderCell>
          <TableHeaderCell />
        </tr>
      </thead>
      <tbody>
        {resources.map((resource) => (
          <tr key={resource.id}>
            <TableCell className="text-ink font-medium">{resource.name}</TableCell>
            <TableCell>{resource.type}</TableCell>
            <TableCell>{resource.activeSlotCount}</TableCell>
            <TableCell>
              <Link href={`/admin/resources/${resource.id}`}>
                <Badge tone="blue" className="cursor-pointer">
                  Manage
                </Badge>
              </Link>
            </TableCell>
          </tr>
        ))}
      </tbody>
    </Table>
  );
}
