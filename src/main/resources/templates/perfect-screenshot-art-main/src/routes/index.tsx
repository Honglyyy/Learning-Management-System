import { createFileRoute, useNavigate } from "@tanstack/react-router";
import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { useMemo, useState } from "react";
import { SiteHeader } from "@/components/SiteHeader";
import { api, mediaUrl } from "@/lib/api";
import { useAuth } from "@/lib/auth";
import { ApiAlert } from "@/components/ApiAlert";
import { toast } from "sonner";
import {
  Search,
  Clock,
  User,
  ArrowRight,
  BookOpen,
  Award,
  Users as UsersIcon,
  Zap,
  Star,
  Heart,
  Sparkles,
  TrendingUp,
  Filter,
  Play,
} from "lucide-react";

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
  coverUrl?: string;
  coverPublicId?: string;
  instructorName?: string;
  instructor?: any;
  categories?: Array<{ id: number; category: string }> | string[];
  rating?: number;
  level?: "BEGINNER" | "INTERMEDIATE" | "ADVANCED" | "ALL_LEVELS";
  status?: "DRAFT" | "PUBLISHED" | "ARCHIVED";
  lessonCount?: number;
  enrollmentCount?: number;
  isFavorite?: boolean;
};

const STATS = [
  { icon: BookOpen,  label: "Courses", value: "240+" },
  { icon: UsersIcon, label: "Students", value: "18k+" },
  { icon: Award,     label: "Certificates", value: "6k+" },
  { icon: Zap,       label: "Hours of content", value: "1,400+" },
];

function Index() {
  const nav = useNavigate();
  const qc = useQueryClient();
  const { isAuthenticated } = useAuth();

  const [q, setQ] = useState("");
  const [activeCategory, setActiveCategory] = useState<string>("All");
  const [levelFilter, setLevelFilter] = useState<string>("ALL");
  const [minRatingFilter, setMinRatingFilter] = useState<number>(0);
  const [sortOption, setSortOption] = useState<string>("latest");
  const [activeFeed, setActiveFeed] = useState<"all" | "featured" | "popular" | "favorites">("all");
  const [visibleCount, setVisibleCount] = useState(6);

  const continueQuery = useQuery<any>({
    queryKey: ["continue-learning"],
    queryFn: () => api("/api/learning/continue", { auth: true }).catch(() => null),
    enabled: isAuthenticated,
  });

  // Main Courses Query
  const { data, isLoading, error } = useQuery<Course[]>({
    queryKey: ["courses", q, activeCategory, levelFilter, minRatingFilter, sortOption, activeFeed, isAuthenticated],
    queryFn: () => {
      if (activeFeed === "featured") {
        return api<Course[]>("/api/courses/featured", { auth: isAuthenticated });
      }
      if (activeFeed === "popular") {
        return api<Course[]>("/api/courses/popular", { auth: isAuthenticated });
      }
      if (activeFeed === "favorites") {
        return api<Course[]>("/api/favorites", { auth: true });
      }
      return api<Course[]>("/api/courses", {
        auth: isAuthenticated,
        query: {
          query: q.trim() || undefined,
          level: levelFilter !== "ALL" ? levelFilter : undefined,
          minRating: minRatingFilter > 0 ? minRatingFilter : undefined,
          sort: sortOption,
        },
      });
    },
  });

  const toggleFavoriteMutation = useMutation({
    mutationFn: async ({ courseId, isFav }: { courseId: number; isFav: boolean }) => {
      if (isFav) {
        await api(`/api/favorites/${courseId}`, { method: "DELETE", auth: true });
      } else {
        await api(`/api/favorites/${courseId}`, { method: "POST", auth: true });
      }
    },
    onSuccess: (_, { isFav }) => {
      toast.success(isFav ? "Removed from saved courses" : "Saved to your courses!");
      qc.invalidateQueries({ queryKey: ["courses"] });
      qc.invalidateQueries({ queryKey: ["my-favorites"] });
    },
    onError: (e: any) => toast.error(e.message || "Failed to update wishlist"),
  });

  const handleToggleFavorite = (courseId: number, currentFav: boolean) => {
    if (!isAuthenticated) {
      toast.info("Please sign in to save courses to your wishlist");
      nav({ to: "/login" });
      return;
    }
    toggleFavoriteMutation.mutate({ courseId, isFav: currentFav });
  };

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
    return data.filter((c) => {
      if (activeCategory === "All") return true;
      return (c.categories || []).some((x: any) =>
        (typeof x === "string" ? x : x?.category) === activeCategory
      );
    });
  }, [data, activeCategory]);

  return (
    <>
      <style>{`
        @import url('https://fonts.googleapis.com/css2?family=Playfair+Display:ital,wght@0,400;0,700;1,400&family=DM+Sans:wght@300;400;500;600;700&display=swap');

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

        /* Hero */
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

        /* Courses section */
        .courses-section {
          max-width: 1200px;
          margin: 0 auto;
          padding: 40px 32px 80px;
        }

        /* Feed Pills Navigation */
        .feed-tabs {
          display: flex;
          gap: 10px;
          margin-bottom: 24px;
          border-bottom: 1px solid var(--border);
          padding-bottom: 16px;
          flex-wrap: wrap;
        }
        .feed-btn {
          display: inline-flex;
          align-items: center;
          gap: 6px;
          padding: 8px 18px;
          border-radius: 20px;
          font-size: 13px;
          font-weight: 500;
          cursor: pointer;
          border: 1px solid var(--border);
          background: var(--parchment);
          color: var(--ink);
          transition: all 0.2s;
        }
        .feed-btn:hover {
          border-color: var(--rust);
          color: var(--rust);
        }
        .feed-btn.active {
          background: var(--rust);
          border-color: var(--rust);
          color: #fff;
        }

        .section-header {
          display: flex;
          align-items: baseline;
          justify-content: space-between;
          margin-bottom: 20px;
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

        /* Multi-criteria filter toolbar */
        .filters-toolbar {
          display: flex;
          align-items: center;
          gap: 12px;
          flex-wrap: wrap;
          padding: 14px 18px;
          background: #F0ECE4;
          border: 1px solid var(--border);
          border-radius: 6px;
          margin-bottom: 24px;
        }
        .filter-group {
          display: flex;
          align-items: center;
          gap: 6px;
          font-size: 12px;
          color: var(--ink);
        }
        .filter-select {
          background: #fff;
          border: 1px solid var(--border);
          border-radius: 4px;
          padding: 6px 10px;
          font-size: 12px;
          font-family: 'DM Sans', sans-serif;
          color: var(--ink);
          outline: none;
        }

        /* category chips */
        .category-chips {
          display: flex;
          flex-wrap: wrap;
          gap: 8px;
          margin-bottom: 28px;
        }
        .chip {
          font-family: 'DM Sans', sans-serif;
          font-size: 12px;
          padding: 6px 16px;
          border-radius: 2px;
          border: 1px solid var(--border);
          background: transparent;
          color: var(--ink);
          cursor: pointer;
          transition: all 0.15s;
        }
        .chip:hover {
          border-color: var(--ink);
        }
        .chip.active {
          background: var(--ink);
          color: var(--parchment);
          border-color: var(--ink);
        }

        /* course grid */
        .course-grid {
          display: grid;
          grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
          gap: 28px;
        }
        .course-card {
          background: #fff;
          border: 1px solid var(--border);
          border-radius: 4px;
          overflow: hidden;
          display: flex;
          flex-direction: column;
          cursor: pointer;
          position: relative;
          transition: transform 0.2s, box-shadow 0.2s;
        }
        .course-card:hover {
          transform: translateY(-3px);
          box-shadow: 0 12px 28px rgba(0,0,0,0.07);
        }
        .card-cover {
          position: relative;
          height: 190px;
          background: var(--ink);
          overflow: hidden;
        }
        .card-cover img {
          width: 100%;
          height: 100%;
          object-fit: cover;
          transition: transform 0.3s;
        }
        .course-card:hover .card-cover img {
          transform: scale(1.03);
        }
        .card-cover-empty {
          width: 100%;
          height: 100%;
          display: flex;
          align-items: center;
          justify-content: center;
          color: #7A7570;
          font-size: 12px;
          text-transform: uppercase;
          letter-spacing: 0.08em;
        }

        /* Favorite button overlay */
        .card-fav-btn {
          position: absolute;
          top: 10px;
          right: 10px;
          background: rgba(0,0,0,0.5);
          backdrop-filter: blur(4px);
          border: none;
          border-radius: 50%;
          width: 32px;
          height: 32px;
          display: flex;
          align-items: center;
          justify-content: center;
          cursor: pointer;
          transition: all 0.15s;
          z-index: 5;
        }
        .card-fav-btn:hover {
          transform: scale(1.1);
          background: rgba(0,0,0,0.75);
        }
        .card-fav-btn.active {
          background: rgba(255,255,255,0.95);
        }

        .card-badges {
          position: absolute;
          bottom: 10px;
          left: 10px;
          right: 10px;
          display: flex;
          justify-content: space-between;
          align-items: center;
          pointer-events: none;
        }
        .card-categories {
          display: flex;
          gap: 6px;
          flex-wrap: wrap;
        }
        .card-category {
          background: rgba(17, 16, 16, 0.82);
          color: var(--parchment);
          font-size: 10px;
          font-weight: 500;
          letter-spacing: 0.06em;
          text-transform: uppercase;
          padding: 3px 8px;
          border-radius: 2px;
          backdrop-filter: blur(4px);
        }
        .card-level-tag {
          background: var(--rust);
          color: #fff;
          font-size: 10px;
          font-weight: 600;
          letter-spacing: 0.06em;
          text-transform: uppercase;
          padding: 3px 8px;
          border-radius: 2px;
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
          font-size: 18px;
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
          font-weight: 600;
          letter-spacing: 0.06em;
          text-transform: uppercase;
          color: var(--rust);
          transition: gap 0.15s;
        }
        .course-card:hover .card-cta {
          gap: 10px;
        }

        /* Show more */
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

        .empty-state {
          padding: 60px 20px;
          text-align: center;
          border: 1px dashed var(--border);
          border-radius: 6px;
          background: #fff;
        }
        .empty-state h3 {
          font-family: 'Playfair Display', serif;
          font-size: 20px;
          margin-bottom: 6px;
        }
        .empty-state p {
          color: var(--muted);
          font-size: 14px;
        }

        /* Footer */
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
          font-size: 26px;
          margin-bottom: 12px;
          color: var(--parchment);
        }
        .footer-brand p {
          font-size: 14px;
          line-height: 1.7;
          color: #7A7570;
          max-width: 280px;
          margin-bottom: 24px;
        }
        .footer-newsletter {
          display: flex;
          max-width: 320px;
        }
        .footer-newsletter input {
          flex: 1;
          background: rgba(255,255,255,0.06);
          border: 1px solid rgba(255,255,255,0.12);
          border-right: none;
          padding: 10px 14px;
          font-family: 'DM Sans', sans-serif;
          font-size: 13px;
          color: var(--parchment);
          outline: none;
        }
        .footer-newsletter input::placeholder { color: #55504A; }
        .footer-newsletter button {
          background: var(--rust);
          color: #fff;
          border: none;
          padding: 10px 18px;
          font-family: 'DM Sans', sans-serif;
          font-size: 12px;
          font-weight: 500;
          cursor: pointer;
        }
        .footer-col h4 {
          font-size: 11px;
          letter-spacing: 0.12em;
          text-transform: uppercase;
          color: #7A7570;
          margin-bottom: 18px;
        }
        .footer-col ul { list-style: none; display: flex; flex-direction: column; gap: 10px; }
        .footer-col a {
          color: #A09890;
          text-decoration: none;
          font-size: 13px;
          transition: color 0.15s;
        }
        .footer-col a:hover { color: var(--parchment); }
        .footer-bottom {
          max-width: 1200px;
          margin: 0 auto;
          padding: 24px 32px;
          border-top: 1px solid rgba(255,255,255,0.07);
          display: flex;
          justify-content: space-between;
          align-items: center;
          font-size: 12px;
          color: #55504A;
          flex-wrap: wrap;
          gap: 12px;
        }
        .footer-legal { display: flex; gap: 20px; }
        .footer-legal a { color: #55504A; text-decoration: none; }
        .footer-legal a:hover { color: #7A7570; }

        @media (max-width: 900px) {
          .hero-inner { grid-template-columns: 1fr; gap: 40px; }
          .hero-stats { padding-bottom: 40px; }
          .footer-top { grid-template-columns: 1fr 1fr; gap: 36px; }
        }
        @media (max-width: 600px) {
          .footer-top { grid-template-columns: 1fr; }
        }
      `}</style>

      <div className="lumen-root">
        <SiteHeader />

        {isAuthenticated && continueQuery.data?.courseId && (
          <div className="bg-[#1c1917] border-b border-[#292524] text-white px-4 py-3">
            <div className="max-w-[1200px] mx-auto flex flex-wrap items-center justify-between gap-3 text-xs">
              <div className="flex items-center gap-2">
                <span className="bg-[#C9512A] text-white px-2 py-0.5 rounded text-[10px] font-bold uppercase tracking-wider">
                  Continue Learning
                </span>
                <span className="font-semibold text-sm">{continueQuery.data.courseTitle}</span>
                <span className="text-[#a8a29e] hidden sm:inline">&bull; Next: {continueQuery.data.nextLessonTitle || "Next lesson"}</span>
              </div>
              <button
                onClick={() => (window.location.href = `/courses/${continueQuery.data.courseId}`)}
                className="bg-[#C9512A] hover:bg-[#b04522] text-white px-3 py-1.5 rounded font-medium transition-colors flex items-center gap-1.5 shadow-sm cursor-pointer"
              >
                <Play size={12} fill="white" /> Resume ({continueQuery.data.progressPercentage ?? 0}%)
              </button>
            </div>
          </div>
        )}

        {/* Hero Section */}
        <section className="hero">
          <div className="hero-inner">
            <div>
              <div className="hero-eyebrow">
                <Sparkles size={13} />
                Lumen Academy
              </div>
              <h1>
                Master skills that <em>shape tomorrow</em>
              </h1>
              <p className="hero-sub">
                Taught by industry pioneers. Built for curious minds ready to lead in design, engineering, and product.
              </p>

              <div className="search-bar">
                <Search size={16} color="#70675f" />
                <input
                  type="text"
                  placeholder="Search by title, instructor, keyword..."
                  value={q}
                  onChange={(e) => resetAndSetQ(e.target.value)}
                />
                <button className="search-btn" onClick={() => setVisibleCount(6)}>
                  Search
                </button>
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
            <span>Course Catalog</span>
          </div>
        </section>

        {/* Main Courses Section */}
        <section className="courses-section">
          {/* Feed Switcher Tabs (Requirement #7: Home feeds) */}
          <div className="feed-tabs">
            <button
              className={`feed-btn ${activeFeed === "all" ? "active" : ""}`}
              onClick={() => { setActiveFeed("all"); setVisibleCount(6); }}
            >
              <BookOpen size={14} /> All Courses
            </button>
            <button
              className={`feed-btn ${activeFeed === "featured" ? "active" : ""}`}
              onClick={() => { setActiveFeed("featured"); setVisibleCount(6); }}
            >
              <Sparkles size={14} /> Featured
            </button>
            <button
              className={`feed-btn ${activeFeed === "popular" ? "active" : ""}`}
              onClick={() => { setActiveFeed("popular"); setVisibleCount(6); }}
            >
              <TrendingUp size={14} /> Most Popular
            </button>
            {isAuthenticated && (
              <button
                className={`feed-btn ${activeFeed === "favorites" ? "active" : ""}`}
                onClick={() => { setActiveFeed("favorites"); setVisibleCount(6); }}
              >
                <Heart size={14} className={activeFeed === "favorites" ? "fill-white" : "fill-rose-500 text-rose-500"} /> Saved Wishlist
              </button>
            )}
          </div>

          <div className="section-header">
            <h2 className="section-title">
              {activeFeed === "featured"
                ? "Featured Courses"
                : activeFeed === "popular"
                ? "Popular Courses"
                : activeFeed === "favorites"
                ? "My Saved Wishlist"
                : "Course Directory"}
            </h2>
            {!isLoading && (
              <span className="result-count">
                {filtered.length} course{filtered.length !== 1 ? "s" : ""}
                {activeCategory !== "All" ? ` in ${activeCategory}` : ""}
              </span>
            )}
          </div>

          {/* Multi-Criteria Filter & Sort Toolbar (Requirement #13) */}
          {activeFeed === "all" && (
            <div className="filters-toolbar">
              <div className="filter-group">
                <Filter size={14} className="text-muted" />
                <span className="font-medium">Filter:</span>
              </div>

              {/* Course Level Filter */}
              <div className="filter-group">
                <label className="text-xs text-muted">Level:</label>
                <select
                  className="filter-select"
                  value={levelFilter}
                  onChange={(e) => { setLevelFilter(e.target.value); setVisibleCount(6); }}
                >
                  <option value="ALL">All Levels</option>
                  <option value="BEGINNER">Beginner</option>
                  <option value="INTERMEDIATE">Intermediate</option>
                  <option value="ADVANCED">Advanced</option>
                </select>
              </div>

              {/* Rating Filter */}
              <div className="filter-group">
                <label className="text-xs text-muted">Rating:</label>
                <select
                  className="filter-select"
                  value={minRatingFilter}
                  onChange={(e) => { setMinRatingFilter(Number(e.target.value)); setVisibleCount(6); }}
                >
                  <option value="0">Any Rating</option>
                  <option value="4">4.0+ Stars</option>
                  <option value="3">3.0+ Stars</option>
                </select>
              </div>

              {/* Sort Filter */}
              <div className="filter-group" style={{ marginLeft: "auto" }}>
                <label className="text-xs text-muted">Sort by:</label>
                <select
                  className="filter-select"
                  value={sortOption}
                  onChange={(e) => { setSortOption(e.target.value); setVisibleCount(6); }}
                >
                  <option value="latest">Latest Released</option>
                  <option value="popular">Most Enrolled</option>
                  <option value="rating">Highest Rated</option>
                  <option value="price_asc">Price: Low to High</option>
                  <option value="price_desc">Price: High to Low</option>
                </select>
              </div>
            </div>
          )}

          {/* Category Chips (Requirement #8) */}
          {activeFeed === "all" && allCategories.length > 1 && (
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
          )}

          <ApiAlert error={error} />

          {isLoading ? (
            <div className="course-grid">
              {Array.from({ length: 6 }).map((_, i) => (
                <div className="course-card" key={i} style={{ height: "340px", background: "#EAE5DC" }} />
              ))}
            </div>
          ) : filtered.length === 0 ? (
            <div className="empty-state">
              <h3>No courses found</h3>
              <p>Try adjusting your search keyword, category, or level filters.</p>
            </div>
          ) : (
            <>
              <div className="course-grid">
                {filtered.slice(0, visibleCount).map((c) => (
                  <CourseCard
                    key={c.courseId}
                    c={c}
                    onOpen={() => nav({ to: "/courses/$id", params: { id: String(c.courseId) } })}
                    onToggleFavorite={handleToggleFavorite}
                  />
                ))}
              </div>

              {filtered.length > visibleCount && (
                <div className="show-more-row">
                  <span className="show-more-count">
                    Showing {Math.min(visibleCount, filtered.length)} of {filtered.length} courses
                  </span>
                  <div className="show-more-actions">
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
                  </div>
                </div>
              )}
            </>
          )}
        </section>

        {/* Footer */}
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
                <li><a href="/">All Courses</a></li>
                <li><a href="/verify-certificate">Verify Certificate</a></li>
                <li><a href="/my/enrollments">My Learning</a></li>
              </ul>
            </div>

            <div className="footer-col">
              <h4>Platform</h4>
              <ul>
                <li><a href="/instructors">Instructors</a></li>
                <li><a href="/verify-certificate">Certificate Lookup</a></li>
              </ul>
            </div>

            <div className="footer-col">
              <h4>Support</h4>
              <ul>
                <li><a href="#">Help Centre</a></li>
                <li><a href="#">Contact Us</a></li>
                <li><a href="#">Community</a></li>
              </ul>
            </div>
          </div>

          <div className="footer-bottom">
            <p>© {new Date().getFullYear()} Lumen LMS. All rights reserved.</p>
            <div className="footer-legal">
              <a href="#">Privacy Policy</a>
              <a href="#">Terms of Service</a>
            </div>
          </div>
        </footer>
      </div>
    </>
  );
}

function CourseCard({
  c,
  onOpen,
  onToggleFavorite,
}: {
  c: Course;
  onOpen: () => void;
  onToggleFavorite?: (courseId: number, currentFav: boolean) => void;
}) {
  const cover = mediaUrl(c.coverUrl || c.coverDir);
  const inst =
    typeof c.instructor === "object"
      ? c.instructor?.fullName || c.instructor?.username || c.instructor?.email
      : c.instructor || c.instructorName;
  const cats = (() => {
    const rawList = c.categories || [];
    return rawList.map((raw: any) => (typeof raw === "string" ? raw : raw?.category)).filter(Boolean);
  })();
  const isFree = !c.price || Number(c.price) === 0;

  return (
    <div
      className="course-card"
      onClick={onOpen}
      role="button"
      tabIndex={0}
      onKeyDown={(e) => e.key === "Enter" && onOpen()}
    >
      <div className="card-cover">
        {cover ? (
          <img src={cover} alt={c.title} loading="lazy" />
        ) : (
          <div className="card-cover-empty">No preview</div>
        )}

        {/* Favorite Heart Button */}
        <button
          type="button"
          className={`card-fav-btn ${c.isFavorite ? "active" : ""}`}
          title={c.isFavorite ? "Remove from wishlist" : "Add to wishlist"}
          onClick={(e) => {
            e.stopPropagation();
            onToggleFavorite?.(c.courseId, !!c.isFavorite);
          }}
        >
          <Heart
            size={16}
            className={c.isFavorite ? "fill-rose-500 text-rose-500" : "text-white"}
          />
        </button>

        <div className="card-badges">
          <div className="card-categories">
            {cats.slice(0, 2).map((cat: string) => (
              <span key={cat} className="card-category">
                {cat}
              </span>
            ))}
          </div>
          {c.level && (
            <span className="card-level-tag">
              {c.level.replace("_", " ")}
            </span>
          )}
        </div>
      </div>

      <div className="card-body">
        <div className="card-title">{c.title}</div>
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
          {c.rating != null && Number(c.rating) > 0 && (
            <span className="card-meta-item text-amber-700 font-medium">
              <Star size={11} className="fill-amber-500 text-amber-500" /> {Number(c.rating).toFixed(1)}
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
