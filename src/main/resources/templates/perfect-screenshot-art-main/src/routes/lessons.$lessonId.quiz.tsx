import { createFileRoute, useNavigate, useParams } from "@tanstack/react-router";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { useState } from "react";
import { ArrowLeft, CheckCircle2, CircleAlert, HelpCircle, Trophy } from "lucide-react";
import { toast } from "sonner";

import { ApiAlert } from "@/components/ApiAlert";
import { SiteHeader } from "@/components/SiteHeader";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Label } from "@/components/ui/label";
import { RadioGroup, RadioGroupItem } from "@/components/ui/radio-group";
import { Skeleton } from "@/components/ui/skeleton";
import { api } from "@/lib/api";
import { cn } from "@/lib/utils";

type Answer = {
  answerId: number;
  answerText: string;
  isCorrect: boolean;
};

type Question = {
  questionId: number;
  questionText: string;
  point?: number | null;
  answers: Answer[];
};

type LessonQuiz = {
  quizId: number;
  title: string;
  totalPoints: number;
  question: Question[];
};

type QuizAttempt = {
  attemptId: number;
  quizId: number;
  enrollmentId: number;
  earnedPoints: number;
  totalPoints: number;
  correctAnswers: number;
  totalQuestions: number;
  submittedAt: string;
  updatedAt?: string;
};

export const Route = createFileRoute("/lessons/$lessonId/quiz")({
  component: LessonQuizPage,
});

function LessonQuizPage() {
  const { lessonId } = useParams({ from: "/lessons/$lessonId/quiz" });
  const nav = useNavigate();
  const qc = useQueryClient();
  const [selectedAnswers, setSelectedAnswers] = useState<Record<number, number>>({});
  const [submitted, setSubmitted] = useState(false);

  const quiz = useQuery<LessonQuiz>({
    queryKey: ["lesson-quiz", lessonId],
    queryFn: () => api(`/api/lessons/${lessonId}/quiz`, { auth: true }),
  });

  const savedAttempt = useQuery<QuizAttempt | null>({
    queryKey: ["quiz-attempt", quiz.data?.quizId],
    enabled: !!quiz.data?.quizId,
    queryFn: () =>
      api<QuizAttempt>(`/api/quizzes/${quiz.data?.quizId}/attempt/me`, { auth: true }).catch(
        () => null,
      ),
  });

  const submitQuiz = useMutation({
    mutationFn: () =>
      api<QuizAttempt>(`/api/quizzes/${quiz.data?.quizId}/submit`, {
        method: "POST",
        auth: true,
        body: { answerIds: Object.values(selectedAnswers) },
      }),
    onSuccess: (attempt) => {
      setSubmitted(true);
      qc.setQueryData(["quiz-attempt", attempt.quizId], attempt);
      toast.success("Quiz score saved");
    },
    onError: () => toast.error("Failed to save quiz score"),
  });

  const questions = quiz.data?.question ?? [];
  const answeredCount = Object.keys(selectedAnswers).length;
  const previewCorrectAnswers = questions.reduce((total, question) => {
    const selectedAnswerId = selectedAnswers[question.questionId];
    const answer = question.answers.find((item) => item.answerId === selectedAnswerId);
    return answer?.isCorrect ? total + 1 : total;
  }, 0);
  const attempt = submitQuiz.data ?? savedAttempt.data;

  return (
    <div className="min-h-screen bg-background">
      <SiteHeader />

      <main className="container mx-auto max-w-4xl px-4 py-10">
        <Button variant="ghost" className="mb-6 gap-2" onClick={() => history.back()}>
          <ArrowLeft className="h-4 w-4" />
          Back to lesson
        </Button>

        {quiz.isLoading ? (
          <div className="space-y-4">
            <Skeleton className="h-24 w-full" />
            <Skeleton className="h-52 w-full" />
          </div>
        ) : quiz.error ? (
          <Card>
            <CardContent className="space-y-4 p-6">
              <ApiAlert error={quiz.error} />
              <p className="text-sm text-muted-foreground">
                This lesson does not have a quiz yet, or you need to sign in before opening it.
              </p>
              <Button onClick={() => nav({ to: "/login" })}>Sign in</Button>
            </CardContent>
          </Card>
        ) : (
          <div className="space-y-6">
            <Card className="overflow-hidden border-primary/20">
              <CardHeader className="bg-primary/5">
                <div className="flex flex-col gap-3 sm:flex-row sm:items-start sm:justify-between">
                  <div>
                    <Badge variant="secondary" className="mb-3 gap-1">
                      <HelpCircle className="h-3.5 w-3.5" />
                      Lesson Quiz
                    </Badge>
                    <CardTitle className="text-2xl">{quiz.data?.title}</CardTitle>
                    <p className="mt-2 text-sm text-muted-foreground">
                      Answer each question, then submit to check your score.
                    </p>
                  </div>
                  <Badge className="w-fit text-sm">
                    {quiz.data?.totalPoints ?? questions.length} points
                  </Badge>
                </div>
              </CardHeader>
            </Card>

            {questions.length === 0 ? (
              <Card>
                <CardContent className="p-6 text-sm text-muted-foreground">
                  No questions have been added to this quiz yet.
                </CardContent>
              </Card>
            ) : (
              questions.map((question, index) => (
                <Card key={question.questionId}>
                  <CardHeader>
                    <div className="flex items-start justify-between gap-4">
                      <CardTitle className="text-base">
                        {index + 1}. {question.questionText}
                      </CardTitle>
                      {question.point != null && (
                        <Badge variant="outline" className="shrink-0 text-xs">
                          {question.point} {question.point === 1 ? "pt" : "pts"}
                        </Badge>
                      )}
                    </div>
                  </CardHeader>
                  <CardContent>
                    <RadioGroup
                      value={String(selectedAnswers[question.questionId] ?? "")}
                    onValueChange={(value) => {
                        setSelectedAnswers((current) => ({
                          ...current,
                          [question.questionId]: Number(value),
                        }));
                        setSubmitted(false);
                      }}
                    >
                      {question.answers.map((answer) => {
                        const selected = selectedAnswers[question.questionId] === answer.answerId;
                        const showCorrect = submitted && answer.isCorrect;
                        const showWrong = submitted && selected && !answer.isCorrect;

                        return (
                          <Label
                            key={answer.answerId}
                            className={cn(
                              "flex cursor-pointer items-center gap-3 rounded-lg border p-3 text-sm transition-colors",
                              selected && "border-primary bg-primary/5",
                              showCorrect && "border-green-600 bg-green-50 text-green-900",
                              showWrong && "border-red-600 bg-red-50 text-red-900",
                            )}
                          >
                            <RadioGroupItem value={String(answer.answerId)} />
                            <span className="flex-1">{answer.answerText}</span>
                            {showCorrect && <CheckCircle2 className="h-4 w-4 text-green-700" />}
                            {showWrong && <CircleAlert className="h-4 w-4 text-red-700" />}
                          </Label>
                        );
                      })}
                    </RadioGroup>
                  </CardContent>
                </Card>
              ))
            )}

            {questions.length > 0 && (
              <Card>
                <CardContent className="flex flex-col gap-4 p-5 sm:flex-row sm:items-center sm:justify-between">
                  <div className="text-sm text-muted-foreground">
                    {attempt ? (
                      <div className="space-y-1">
                        <span className="flex items-center gap-2 font-medium text-foreground">
                          <Trophy className="h-4 w-4 text-primary" />
                          Saved points: {formatPoints(attempt.earnedPoints)} /{" "}
                          {formatPoints(attempt.totalPoints)} ({attempt.correctAnswers} /{" "}
                          {attempt.totalQuestions} correct)
                        </span>
                        {attempt.submittedAt && (
                          <span className="text-xs text-muted-foreground block">
                            Submitted on {new Date(attempt.submittedAt).toLocaleString()}
                          </span>
                        )}
                      </div>
                    ) : submitted ? (
                      <span className="flex items-center gap-2 font-medium text-foreground">
                        <Trophy className="h-4 w-4 text-primary" />
                        Correct: {previewCorrectAnswers} / {questions.length}
                      </span>
                    ) : (
                      <span>
                        Answered {answeredCount} / {questions.length}
                      </span>
                    )}
                  </div>
                  <Button
                    disabled={answeredCount !== questions.length || submitQuiz.isPending}
                    onClick={() => submitQuiz.mutate()}
                  >
                    {submitQuiz.isPending ? "Saving..." : attempt ? "Retake and save" : "Submit quiz"}
                  </Button>
                </CardContent>
              </Card>
            )}
          </div>
        )}
      </main>
    </div>
  );
}

function formatPoints(points: number) {
  return Number.isInteger(points) ? String(points) : points.toFixed(2);
}
