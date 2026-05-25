import { createFileRoute, useNavigate } from "@tanstack/react-router";
import { useQuery } from "@tanstack/react-query";
import { useMemo, useState } from "react";
import { SiteHeader } from "@/components/SiteHeader";
import { api, mediaUrl } from "@/lib/api";
import { ApiAlert } from "@/components/ApiAlert";
import { Skeleton } from "@/components/ui/skeleton";
import { Search, Clock, User, ArrowRight, BookOpen, Award, Users, Zap } from "lucide-react";

export const Route = createFileRoute("/")({
  head: () => ({
    meta: [
      { title: "Browse courses — Lumen LMS" },
      { name: "description", content: "Discover and enroll in expert-led courses." },
    ],
  }),
  component: Index,
});

type Course = {
  courseId: number;
  title: string;
  description?: string;
  price?: number;
  overallDuration?: string;
  coverDir?: string;
  instructorName?: string;
  instructor?: any;
  categories?: Array<{ id: number; category: string }> | string[];
  rating?: number;
};

const STATS = [
  { icon: BookOpen, label: "Courses", value: "240+" },
  { icon: Users,    label: "Students", value: "18k+" },
  { icon: Award,    label: "Certificates", value: "6k+" },
  { icon: Zap,      label: "Hours of content", value: "1,400+" },
];

function Index() {
  const nav = useNavigate();
  const [q, setQ] = useState("");
  const [activeCategory, setActiveCategory] = useState<string>("All");
  const [visibleCount, setVisibleCount] = useState(6);

  const { data, isLoading, error } = useQuery<Course[]>({
    queryKey: ["courses"],
    queryFn: () => api<Course[]>("/api/courses", { auth: false }),
  });

  // Reset pagination when filters change
  const resetAndSetQ = (val: string) => { setQ(val); setVisibleCount(6); };
  const resetAndSetCategory = (val: string) => { setActiveCategory(val); setVisibleCount(6); };

  const allCategories = useMemo(() => {
    if (!data) return ["All"];
    const cats = new Set<string>();
    data.forEach((c) =>
        (c.categories || []).forEach((x: any) =>
            cats.add(typeof x === "string" ? x : x?.category)
        )
    );
    return ["All", ...Array.from(cats).filter(Boolean).sort()];
  }, [data]);

  const filtered = useMemo(() => {
    if (!data) return [];
    const needle = q.trim().toLowerCase();
    return data.filter((c) => {
      const inst =
          typeof c.instructor === "object"
              ? c.instructor?.username || c.instructor?.email
              : c.instructorName;
      const cats = (c.categories || [])
          .map((x: any) => (typeof x === "string" ? x : x?.category))
          .join(" ");

      const matchesSearch = !needle ||
          [c.title, c.description, inst, cats]
              .filter(Boolean)
              .join(" ")
              .toLowerCase()
              .includes(needle);

      const matchesCategory =
          activeCategory === "All" ||
          (c.categories || []).some((x: any) =>
              (typeof x === "string" ? x : x?.category) === activeCategory
          );

      return matchesSearch && matchesCategory;
    });
  }, [data, q, activeCategory]);

  return (
      <>
        <style>{`
        @import url('https://fonts.googleapis.com/css2?family=Playfair+Display:ital,wght@0,400;0,700;1,400&family=DM+Sans:wght@300;400;500&display=swap');

        .lumen-root {
          --ink: #111010;
          --parchment: #F5F0E8;
          --cream: #FAF7F2;
          --rust: #C9512A;
          --rust-light: #F0D5C8;
          --muted: #7A7570;
          --border: #DDD8CF;
          font-family: 'DM Sans', sans-serif;
          background: var(--cream);
          color: var(--ink);
          min-height: 100vh;
        }

        .lumen-root * { box-sizing: border-box; margin: 0; padding: 0; }

        /* ── Hero ── */
        .hero {
          background: var(--ink);
          color: var(--parchment);
          padding: 80px 0 0;
          overflow: hidden;
          position: relative;
        }
        .hero::before {
          content: '';
          position: absolute;
          inset: 0;
          background: radial-gradient(ellipse 60% 80% at 70% 50%, #2a1f1a 0%, transparent 70%);
          pointer-events: none;
        }
        .hero-inner {
          max-width: 1200px;
          margin: 0 auto;
          padding: 0 32px;
          display: grid;
          grid-template-columns: 1fr 1fr;
          gap: 64px;
          align-items: end;
        }
        .hero-eyebrow {
          display: inline-flex;
          align-items: center;
          gap: 6px;
          font-size: 11px;
          font-weight: 500;
          letter-spacing: 0.12em;
          text-transform: uppercase;
          color: var(--rust);
          border: 1px solid var(--rust);
          padding: 5px 12px;
          border-radius: 2px;
          margin-bottom: 24px;
        }
        .hero h1 {
          font-family: 'Playfair Display', serif;
          font-size: clamp(40px, 5vw, 64px);
          line-height: 1.1;
          color: var(--parchment);
          margin-bottom: 20px;
        }
        .hero h1 em {
          font-style: italic;
          color: var(--rust);
        }
        .hero-sub {
          font-size: 16px;
          line-height: 1.7;
          color: #A09890;
          max-width: 440px;
          margin-bottom: 36px;
        }
        .search-bar {
          display: flex;
          align-items: center;
          background: rgba(255,255,255,0.07);
          border: 1px solid rgba(255,255,255,0.15);
          border-radius: 4px;
          padding: 4px 4px 4px 16px;
          gap: 8px;
          transition: border-color 0.2s;
        }
        .search-bar:focus-within {
          border-color: var(--rust);
        }
        .search-bar input {
          flex: 1;
          background: transparent;
          border: none;
          outline: none;
          color: var(--parchment);
          font-family: 'DM Sans', sans-serif;
          font-size: 14px;
        }
        .search-bar input::placeholder { color: #70675f; }
        .search-btn {
          background: var(--rust);
          color: #fff;
          border: none;
          border-radius: 2px;
          padding: 10px 20px;
          font-family: 'DM Sans', sans-serif;
          font-size: 13px;
          font-weight: 500;
          cursor: pointer;
          letter-spacing: 0.04em;
          transition: opacity 0.15s;
        }
        .search-btn:hover { opacity: 0.88; }

        /* stats strip */
        .hero-stats {
          align-self: end;
          padding-bottom: 56px;
          display: grid;
          grid-template-columns: 1fr 1fr;
          gap: 2px;
        }
        .stat-box {
          background: rgba(255,255,255,0.04);
          border: 1px solid rgba(255,255,255,0.07);
          padding: 24px 20px;
          text-align: center;
        }
        .stat-value {
          font-family: 'Playfair Display', serif;
          font-size: 32px;
          color: var(--parchment);
          line-height: 1;
          margin-bottom: 4px;
        }
        .stat-label {
          font-size: 11px;
          letter-spacing: 0.08em;
          text-transform: uppercase;
          color: #7A7570;
        }

        /* dividing rule */
        .hero-rule {
          max-width: 1200px;
          margin: 0 auto;
          padding: 0 32px;
          border-top: 1px solid rgba(255,255,255,0.08);
          margin-top: 56px;
          display: flex;
          align-items: center;
          gap: 8px;
          padding-top: 16px;
          padding-bottom: 16px;
        }
        .hero-rule span {
          font-size: 11px;
          letter-spacing: 0.1em;
          text-transform: uppercase;
          color: #55504A;
        }
        .hero-rule::after {
          content: '';
          flex: 1;
          height: 1px;
          background: rgba(255,255,255,0.06);
        }

        /* ── Courses section ── */
        .courses-section {
          max-width: 1200px;
          margin: 0 auto;
          padding: 56px 32px 80px;
        }
        .section-header {
          display: flex;
          align-items: baseline;
          justify-content: space-between;
          margin-bottom: 28px;
          gap: 16px;
          flex-wrap: wrap;
        }
        .section-title {
          font-family: 'Playfair Display', serif;
          font-size: 28px;
          font-weight: 700;
          color: var(--ink);
        }
        .result-count {
          font-size: 13px;
          color: var(--muted);
        }

        /* category chips */
        .category-chips {
          display: flex;
          flex-wrap: wrap;
          gap: 8px;
          margin-bottom: 36px;
        }
        .chip {
          font-family: 'DM Sans', sans-serif;
          font-size: 12px;
          font-weight: 500;
          letter-spacing: 0.04em;
          padding: 6px 14px;
          border-radius: 2px;
          border: 1px solid var(--border);
          background: transparent;
          color: var(--muted);
          cursor: pointer;
          transition: all 0.15s;
        }
        .chip:hover {
          border-color: var(--ink);
          color: var(--ink);
        }
        .chip.active {
          background: var(--ink);
          border-color: var(--ink);
          color: var(--parchment);
        }

        /* grid */
        .course-grid {
          display: grid;
          gap: 1px;
          grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
          background: var(--border);
          border: 1px solid var(--border);
        }

        /* card */
        .course-card {
          background: var(--cream);
          display: flex;
          flex-direction: column;
          cursor: pointer;
          transition: background 0.18s;
          position: relative;
        }
        .course-card:hover { background: var(--parchment); }
        .course-card:hover .card-arrow { opacity: 1; transform: translate(2px, -2px); }

        .card-cover {
          aspect-ratio: 16/9;
          overflow: hidden;
          background: #E8E2D8;
          position: relative;
        }
        .card-cover img {
          width: 100%;
          height: 100%;
          object-fit: cover;
          transition: transform 0.4s ease;
        }
        .course-card:hover .card-cover img { transform: scale(1.04); }
        .card-cover-empty {
          display: flex;
          align-items: center;
          justify-content: center;
          height: 100%;
          color: #B0A89A;
          font-size: 12px;
          letter-spacing: 0.06em;
          text-transform: uppercase;
        }

        .card-category {
          position: absolute;
          top: 12px;
          left: 12px;
          background: var(--ink);
          color: var(--parchment);
          font-size: 10px;
          font-weight: 500;
          letter-spacing: 0.1em;
          text-transform: uppercase;
          padding: 4px 8px;
          border-radius: 1px;
        }

        .card-body {
          padding: 20px 22px 14px;
          flex: 1;
          display: flex;
          flex-direction: column;
          gap: 8px;
        }
        .card-title {
          font-family: 'Playfair Display', serif;
          font-size: 17px;
          line-height: 1.35;
          font-weight: 700;
          color: var(--ink);
          display: -webkit-box;
          -webkit-line-clamp: 2;
          -webkit-box-orient: vertical;
          overflow: hidden;
        }
        .card-desc {
          font-size: 13px;
          line-height: 1.6;
          color: var(--muted);
          display: -webkit-box;
          -webkit-line-clamp: 2;
          -webkit-box-orient: vertical;
          overflow: hidden;
        }
        .card-meta {
          display: flex;
          align-items: center;
          gap: 14px;
          font-size: 12px;
          color: #9A9088;
          margin-top: 4px;
          flex-wrap: wrap;
        }
        .card-meta-item {
          display: flex;
          align-items: center;
          gap: 4px;
        }

        .card-footer {
          padding: 14px 22px 18px;
          display: flex;
          align-items: center;
          justify-content: space-between;
          border-top: 1px solid var(--border);
          margin-top: auto;
        }
        .card-price {
          font-family: 'Playfair Display', serif;
          font-size: 22px;
          font-weight: 700;
          color: var(--ink);
        }
        .card-price-free {
          color: #2E7D32;
        }
        .card-cta {
          display: flex;
          align-items: center;
          gap: 6px;
          font-size: 12px;
          font-weight: 500;
          letter-spacing: 0.06em;
          text-transform: uppercase;
          color: var(--rust);
        }
        .card-arrow {
          opacity: 0;
          transition: opacity 0.18s, transform 0.18s;
        }

        /* empty state */
        .empty-state {
          text-align: center;
          padding: 80px 20px;
          color: var(--muted);
        }
        .empty-state h3 {
          font-family: 'Playfair Display', serif;
          font-size: 22px;
          color: var(--ink);
          margin-bottom: 8px;
        }

        /* skeletons */
        .skeleton-grid {
          display: grid;
          gap: 1px;
          grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
          background: var(--border);
          border: 1px solid var(--border);
        }
        .skeleton-card {
          background: var(--cream);
          padding: 0;
        }
        .skeleton-img {
          aspect-ratio: 16/9;
          background: linear-gradient(90deg, #E8E2D8 25%, #F0EBE2 50%, #E8E2D8 75%);
          background-size: 200% 100%;
          animation: shimmer 1.4s infinite;
        }
        .skeleton-body { padding: 20px 22px; display: flex; flex-direction: column; gap: 10px; }
        .skeleton-line {
          height: 14px;
          border-radius: 2px;
          background: linear-gradient(90deg, #E0D9CE 25%, #EDE7DC 50%, #E0D9CE 75%);
          background-size: 200% 100%;
          animation: shimmer 1.4s infinite;
        }
        @keyframes shimmer {
          0% { background-position: 200% 0; }
          100% { background-position: -200% 0; }
        }

        /* ── Show more ── */
        .show-more-row {
          margin-top: 40px;
          display: flex;
          align-items: center;
          gap: 16px;
          flex-wrap: wrap;
          border-top: 1px solid var(--border);
          padding-top: 32px;
        }
        .show-more-count {
          font-size: 13px;
          color: var(--muted);
          flex: 1;
        }
        .show-more-actions {
          display: flex;
          gap: 10px;
          flex-wrap: wrap;
        }
        .show-more-btn {
          font-family: 'DM Sans', sans-serif;
          font-size: 12px;
          font-weight: 500;
          letter-spacing: 0.06em;
          text-transform: uppercase;
          padding: 10px 22px;
          border-radius: 2px;
          cursor: pointer;
          transition: all 0.15s;
          background: transparent;
          border: 1px solid var(--ink);
          color: var(--ink);
        }
        .show-more-btn:hover {
          background: var(--ink);
          color: var(--parchment);
        }
        .show-all-btn {
          background: var(--rust);
          border-color: var(--rust);
          color: #fff;
        }
        .show-all-btn:hover {
          opacity: 0.88;
          background: var(--rust);
          color: #fff;
        }

        /* ── Footer ── */
        .site-footer {
          background: var(--ink);
          color: var(--parchment);
        }
        .footer-top {
          max-width: 1200px;
          margin: 0 auto;
          padding: 64px 32px 48px;
          display: grid;
          grid-template-columns: 2fr 1fr 1fr 1fr;
          gap: 48px;
        }
        .footer-brand h2 {
          font-family: 'Playfair Display', serif;
          font-size: 24px;
          font-weight: 700;
          color: var(--parchment);
          margin-bottom: 12px;
        }
        .footer-brand p {
          font-size: 13px;
          line-height: 1.7;
          color: #7A7570;
          max-width: 280px;
          margin-bottom: 24px;
        }
        .footer-newsletter {
          display: flex;
          gap: 0;
          border: 1px solid rgba(255,255,255,0.12);
        }
        .footer-newsletter input {
          flex: 1;
          background: transparent;
          border: none;
          outline: none;
          padding: 10px 14px;
          font-family: 'DM Sans', sans-serif;
          font-size: 13px;
          color: var(--parchment);
        }
        .footer-newsletter input::placeholder { color: #55504A; }
        .footer-newsletter button {
          background: var(--rust);
          border: none;
          padding: 10px 16px;
          color: #fff;
          font-family: 'DM Sans', sans-serif;
          font-size: 12px;
          font-weight: 500;
          letter-spacing: 0.06em;
          text-transform: uppercase;
          cursor: pointer;
          transition: opacity 0.15s;
        }
        .footer-newsletter button:hover { opacity: 0.85; }

        .footer-col h4 {
          font-size: 10px;
          font-weight: 500;
          letter-spacing: 0.14em;
          text-transform: uppercase;
          color: #55504A;
          margin-bottom: 18px;
        }
        .footer-col ul { list-style: none; display: flex; flex-direction: column; gap: 10px; }
        .footer-col ul li a {
          font-size: 13px;
          color: #7A7570;
          text-decoration: none;
          transition: color 0.15s;
        }
        .footer-col ul li a:hover { color: var(--parchment); }

        .footer-bottom {
          border-top: 1px solid rgba(255,255,255,0.07);
          max-width: 1200px;
          margin: 0 auto;
          padding: 20px 32px;
          display: flex;
          align-items: center;
          justify-content: space-between;
          gap: 16px;
          flex-wrap: wrap;
        }
        .footer-bottom p {
          font-size: 12px;
          color: #45403A;
          letter-spacing: 0.03em;
        }
        .footer-legal {
          display: flex;
          gap: 24px;
        }
        .footer-legal a {
          font-size: 12px;
          color: #45403A;
          text-decoration: none;
          transition: color 0.15s;
        }
        .footer-legal a:hover { color: #7A7570; }

        @media (max-width: 900px) {
          .hero-inner { grid-template-columns: 1fr; gap: 40px; }
          .hero-stats { padding-bottom: 0; }
          .footer-top { grid-template-columns: 1fr 1fr; }
          .footer-brand { grid-column: 1 / -1; }
        }
        @media (max-width: 600px) {
          .hero { padding: 60px 0 0; }
          .footer-top { grid-template-columns: 1fr; }
          .footer-brand { grid-column: auto; }
        }
      `}</style>

        <div className="lumen-root">
          <SiteHeader />

          {/* ── Hero ─────────────────────────────────────── */}
          <section className="hero">
            <div className="hero-inner">
              <div>
                <div className="hero-eyebrow">
                  <span>✦</span> New courses every week
                </div>
                <h1>
                  Learn from<br />
                  the <em>best.</em><br />
                  Build what's next.
                </h1>
                <p className="hero-sub">
                  Hands-on courses in programming, design, and business —
                  taught by practitioners who do the work.
                </p>
                <div className="search-bar">
                  <Search size={15} color="#70675f" />
                  <input
                      placeholder="Search by title, instructor, or category…"
                      value={q}
                      onChange={(e) => resetAndSetQ(e.target.value)}
                  />
                  <button className="search-btn">Search</button>
                </div>
              </div>

              <div className="hero-stats">
                {STATS.map(({ icon: Icon, label, value }) => (
                    <div className="stat-box" key={label}>
                      <div className="stat-value">{value}</div>
                      <div className="stat-label">{label}</div>
                    </div>
                ))}
              </div>
            </div>

            <div className="hero-rule">
              <span>Browse catalogue</span>
            </div>
          </section>

          {/* ── Courses ──────────────────────────────────── */}
          <section className="courses-section">
            <div className="section-header">
              <h2 className="section-title">All Courses</h2>
              {!isLoading && (
                  <span className="result-count">
                {filtered.length} course{filtered.length !== 1 ? "s" : ""}
                    {activeCategory !== "All" ? ` in ${activeCategory}` : ""}
              </span>
              )}
            </div>

            {/* Category chips */}
            <div className="category-chips">
              {allCategories.map((cat) => (
                  <button
                      key={cat}
                      className={`chip${activeCategory === cat ? " active" : ""}`}
                      onClick={() => resetAndSetCategory(cat)}
                  >
                    {cat}
                  </button>
              ))}
            </div>

            <ApiAlert error={error} />

            {isLoading ? (
                <div className="skeleton-grid">
                  {Array.from({ length: 6 }).map((_, i) => (
                      <div className="skeleton-card" key={i}>
                        <div className="skeleton-img" style={{ animationDelay: `${i * 0.1}s` }} />
                        <div className="skeleton-body">
                          <div className="skeleton-line" style={{ width: "70%" }} />
                          <div className="skeleton-line" style={{ width: "90%" }} />
                          <div className="skeleton-line" style={{ width: "50%" }} />
                        </div>
                      </div>
                  ))}
                </div>
            ) : filtered.length === 0 ? (
                <div className="empty-state">
                  <h3>No courses found</h3>
                  <p>Try a different search term or category filter.</p>
                </div>
            ) : (
                <>
                  <div className="course-grid">
                    {filtered.slice(0, visibleCount).map((c) => (
                        <CourseCard
                            key={c.courseId}
                            c={c}
                            onOpen={() =>
                                nav({ to: "/courses/$id", params: { id: String(c.courseId) } })
                            }
                        />
                    ))}
                  </div>

                  {filtered.length > 6 && (
                      <div className="show-more-row">
                  <span className="show-more-count">
                    Showing {Math.min(visibleCount, filtered.length)} of {filtered.length} courses
                  </span>
                        <div className="show-more-actions">
                          {visibleCount < filtered.length ? (
                              <>
                                <button
                                    className="show-more-btn"
                                    onClick={() => setVisibleCount((n) => n + 6)}
                                >
                                  Show 6 more
                                </button>
                                <button
                                    className="show-more-btn show-all-btn"
                                    onClick={() => setVisibleCount(filtered.length)}
                                >
                                  Show all {filtered.length}
                                </button>
                              </>
                          ) : (
                              <button
                                  className="show-more-btn"
                                  onClick={() => {
                                    setVisibleCount(6);
                                    window.scrollTo({ top: 0, behavior: "smooth" });
                                  }}
                              >
                                Show less
                              </button>
                          )}
                        </div>
                      </div>
                  )}
                </>
            )}
          </section>

          {/* ── Footer ───────────────────────────────────── */}
          <footer className="site-footer">
            <div className="footer-top">
              <div className="footer-brand">
                <h2>Lumen</h2>
                <p>
                  Expert-led courses designed for real-world impact.
                  Build skills that employers actually want.
                </p>
                <div className="footer-newsletter">
                  <input type="email" placeholder="Your email address" />
                  <button>Subscribe</button>
                </div>
              </div>

              <div className="footer-col">
                <h4>Learn</h4>
                <ul>
                  <li><a href="#">All Courses</a></li>
                  <li><a href="#">New Releases</a></li>
                  <li><a href="#">Certificates</a></li>
                  <li><a href="#">Learning Paths</a></li>
                  <li><a href="#">Free Courses</a></li>
                </ul>
              </div>

              <div className="footer-col">
                <h4>Company</h4>
                <ul>
                  <li><a href="#">About</a></li>
                  <li><a href="#">Instructors</a></li>
                  <li><a href="#">Careers</a></li>
                  <li><a href="#">Blog</a></li>
                  <li><a href="#">Press</a></li>
                </ul>
              </div>

              <div className="footer-col">
                <h4>Support</h4>
                <ul>
                  <li><a href="#">Help Centre</a></li>
                  <li><a href="#">Contact Us</a></li>
                  <li><a href="#">Community</a></li>
                  <li><a href="#">Teach on Lumen</a></li>
                  <li><a href="#">Accessibility</a></li>
                </ul>
              </div>
            </div>

            <div className="footer-bottom">
              <p>© {new Date().getFullYear()} Lumen LMS. All rights reserved.</p>
              <div className="footer-legal">
                <a href="#">Privacy Policy</a>
                <a href="#">Terms of Service</a>
                <a href="#">Cookie Settings</a>
              </div>
            </div>
          </footer>
        </div>
      </>
  );
}

function CourseCard({ c, onOpen }: { c: Course; onOpen: () => void }) {
  const cover = mediaUrl(c.coverDir);
  const inst =
      typeof c.instructor === "object"
          ? c.instructor?.username || c.instructor?.email
          : c.instructorName;
  const firstCat = (() => {
    const cats = c.categories || [];
    if (!cats.length) return null;
    const raw = cats[0];
    return typeof raw === "string" ? raw : (raw as any)?.category;
  })();
  const isFree = !c.price || Number(c.price) === 0;

  return (
      <div className="course-card" onClick={onOpen} role="button" tabIndex={0}
           onKeyDown={(e) => e.key === "Enter" && onOpen()}>
        <div className="card-cover">
          {cover ? (
              <img src={cover} alt={c.title} loading="lazy" />
          ) : (
              <div className="card-cover-empty">No preview</div>
          )}
          {firstCat && <span className="card-category">{firstCat}</span>}
        </div>

        <div className="card-body">
          <div className="card-title">{c.title}</div>
          {c.instructor && <div className="card-desc"><span className={"text-black"}>Instructor: </span>{c.instructor}</div>}
          {c.description && <div className="card-desc">{c.description}</div>}
          <div className="card-meta">
            {inst && (
                <span className="card-meta-item">
              <User size={11} /> {inst}
            </span>
            )}
            {c.overallDuration && (
                <span className="card-meta-item">
              <Clock size={11} /> {c.overallDuration}
            </span>
            )}
          </div>
        </div>

        <div className="card-footer">
        <span className={`card-price${isFree ? " card-price-free" : ""}`}>
          {isFree ? "Free" : `$${Number(c.price).toFixed(2)}`}
        </span>
          <span className="card-cta">
          Enroll <ArrowRight size={13} className="card-arrow" />
        </span>
        </div>
      </div>
  );
}