const API_BASE_URL =
  process.env.NEXT_PUBLIC_API_BASE_URL ?? "http://localhost:8080";

export type ResourceApiResponse = {
  id: number;
  name: string;
  description: string;
};

export async function getResourceByIdFromApi(id: number): Promise<ResourceApiResponse> {
  const response = await fetch(`${API_BASE_URL}/api/resources/${id}`, {
    method: "GET",
    credentials: "include",
  });

  if (!response.ok) {
    throw new Error(`Failed to fetch resource ${id}: ${response.status}`);
  }

  return response.json();
}