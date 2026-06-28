interface MetricTileProps {
  label: string;
  value: string | number;
}

/**
 * A single label + number tile. Used in rows of three on the User Home
 * and Admin Home screens. Deliberately count-only — the approved design
 * is explicit that these are "a quick read, not a dashboard with charts",
 * matching Section 6 of the project's own spec, which rules out admin
 * analytics dashboards.
 */
export function MetricTile({ label, value }: MetricTileProps) {
  return (
    <div className="border-screen-line flex-1 rounded-lg border bg-white px-4 py-3">
      <div className="text-ink-3 text-xs font-semibold tracking-wide uppercase">{label}</div>
      <div className="text-ink mt-1 text-2xl font-bold">{value}</div>
    </div>
  );
}
