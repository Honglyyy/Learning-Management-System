import { createFileRoute } from "@tanstack/react-router";
import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { useState, useEffect } from "react";
import { api, mediaUrl } from "@/lib/api";
import { useAuth } from "@/lib/auth";
import { RequireAuth } from "@/components/RequireRole";
import { SiteHeader } from "@/components/SiteHeader";
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select";
import { Badge } from "@/components/ui/badge";
import { ApiAlert } from "@/components/ApiAlert";
import { Skeleton } from "@/components/ui/skeleton";
import { toast } from "sonner";
import { User, Camera, Lock, Save, ShieldCheck, GraduationCap, Calendar, IdCard } from "lucide-react";

export const Route = createFileRoute("/profile")({
  head: () => ({ meta: [{ title: "My Profile — Lumen LMS" }] }),
  component: () => (
    <RequireAuth>
      <ProfilePage />
    </RequireAuth>
  ),
});

type StudentProfile = {
  studentId?: number;
  studentCode?: string;
  username?: string;
  email?: string;
  fullName?: string;
  phoneNumber?: string;
  gender?: "MALE" | "FEMALE" | "OTHER" | "NOT_SPECIFIC";
  dateOfBirth?: string;
  educationLevel?: string;
  profilePhotoUrl?: string;
  profilePhotoPublicId?: string;
  createdAt?: string;
  updatedAt?: string;
};

function ProfilePage() {
  const { email, role } = useAuth();
  const qc = useQueryClient();

  const isStudentOrUser = role === "STUDENT" || role === "USER";

  const { data: profile, isLoading, error } = useQuery<StudentProfile>({
    queryKey: ["student-profile"],
    queryFn: () => api<StudentProfile>("/api/students/profile"),
    enabled: isStudentOrUser,
  });

  const [form, setForm] = useState<{
    fullName: string;
    phoneNumber: string;
    gender: "MALE" | "FEMALE" | "OTHER" | "NOT_SPECIFIC";
    dateOfBirth: string;
    educationLevel: string;
  }>({
    fullName: "",
    phoneNumber: "",
    gender: "NOT_SPECIFIC",
    dateOfBirth: "",
    educationLevel: "",
  });

  useEffect(() => {
    if (profile) {
      setForm({
        fullName: profile.fullName || "",
        phoneNumber: profile.phoneNumber || "",
        gender: profile.gender || "NOT_SPECIFIC",
        dateOfBirth: profile.dateOfBirth || "",
        educationLevel: profile.educationLevel || "",
      });
    }
  }, [profile]);

  const [uploadingPhoto, setUploadingPhoto] = useState(false);
  const [photoError, setPhotoError] = useState<unknown>(null);

  // Profile update mutation
  const updateProfile = useMutation({
    mutationFn: (body: any) =>
      api<StudentProfile>("/api/students/profile", {
        method: "PUT",
        body,
      }),
    onSuccess: (updated) => {
      qc.setQueryData(["student-profile"], updated);
      toast.success("Profile updated successfully!");
    },
    onError: (err: any) => {
      toast.error(err.message || "Failed to update profile");
    },
  });

  async function handlePhotoUpload(e: React.ChangeEvent<HTMLInputElement>) {
    const file = e.target.files?.[0];
    if (!file) return;
    setUploadingPhoto(true);
    setPhotoError(null);
    try {
      const fd = new FormData();
      fd.append("file", file);
      const updated = await api<StudentProfile>("/api/students/profile/photo", {
        method: "POST",
        formData: fd,
      });
      qc.setQueryData(["student-profile"], updated);
      toast.success("Profile photo updated!");
    } catch (err) {
      setPhotoError(err);
      toast.error("Failed to upload photo");
    } finally {
      setUploadingPhoto(false);
    }
  }

  // Change password state & mutation
  const [pwForm, setPwForm] = useState({ oldPassword: "", newPassword: "", confirmPassword: "" });
  const [pwError, setPwError] = useState<unknown>(null);

  const changePw = useMutation({
    mutationFn: () => {
      if (pwForm.newPassword !== pwForm.confirmPassword) {
        throw new Error("New passwords do not match");
      }
      return api<string>("/api/users/change-password", {
        method: "POST",
        body: {
          oldPassword: pwForm.oldPassword,
          newPassword: pwForm.newPassword,
        },
      });
    },
    onSuccess: () => {
      toast.success("Password changed successfully");
      setPwForm({ oldPassword: "", newPassword: "", confirmPassword: "" });
      setPwError(null);
    },
    onError: (err: any) => {
      setPwError(err);
    },
  });

  return (
    <div className="min-h-screen bg-background">
      <SiteHeader />
      <main className="container mx-auto max-w-4xl px-4 py-8 space-y-8">
        <div>
          <h1 className="text-3xl font-bold tracking-tight">Account Settings</h1>
          <p className="text-muted-foreground">Manage your personal profile and security preferences.</p>
        </div>

        {isStudentOrUser ? (
          <Card>
            <CardHeader>
              <CardTitle className="flex items-center gap-2">
                <User className="h-5 w-5 text-primary" /> Profile Information
              </CardTitle>
              <CardDescription>Update your personal details and public learning profile.</CardDescription>
            </CardHeader>
            <CardContent className="space-y-6">
              <ApiAlert error={error || photoError} />

              {isLoading ? (
                <div className="space-y-4">
                  <Skeleton className="h-20 w-20 rounded-full" />
                  <Skeleton className="h-10 w-full" />
                  <Skeleton className="h-10 w-full" />
                </div>
              ) : (
                <>
                  {/* Profile Photo & Info */}
                  <div className="flex flex-wrap items-center gap-6">
                    <div className="relative group">
                      <div className="h-24 w-24 rounded-full overflow-hidden border-2 border-border bg-muted flex items-center justify-center">
                        {profile?.profilePhotoUrl ? (
                          <img
                            src={mediaUrl(profile.profilePhotoUrl)}
                            alt="Profile"
                            className="h-full w-full object-cover"
                          />
                        ) : (
                          <User className="h-10 w-10 text-muted-foreground" />
                        )}
                      </div>
                      <label className="absolute bottom-0 right-0 bg-primary text-primary-foreground p-1.5 rounded-full cursor-pointer shadow hover:bg-primary/90 transition-colors">
                        <Camera className="h-4 w-4" />
                        <input
                          type="file"
                          accept="image/*"
                          className="hidden"
                          onChange={handlePhotoUpload}
                          disabled={uploadingPhoto}
                        />
                      </label>
                    </div>
                    <div className="space-y-1">
                      <div className="flex items-center gap-2">
                        <h3 className="font-semibold text-lg text-foreground">
                          {profile?.fullName || profile?.username || "Student"}
                        </h3>
                        {profile?.studentCode && (
                          <Badge variant="outline" className="font-mono text-xs gap-1">
                            <IdCard className="h-3 w-3" />
                            {profile.studentCode}
                          </Badge>
                        )}
                      </div>
                      <p className="text-xs text-muted-foreground">
                        {uploadingPhoto ? "Uploading photo..." : "Upload JPG, PNG or WebP image."}
                      </p>
                      {profile?.createdAt && (
                        <p className="text-xs text-muted-foreground flex items-center gap-1">
                          <Calendar className="h-3 w-3" />
                          Joined {new Date(profile.createdAt).toLocaleDateString()}
                        </p>
                      )}
                    </div>
                  </div>

                  {/* Profile Form */}
                  <form
                    onSubmit={(e) => {
                      e.preventDefault();
                      updateProfile.mutate({
                        fullName: form.fullName,
                        phoneNumber: form.phoneNumber,
                        gender: form.gender,
                        dateOfBirth: form.dateOfBirth || null,
                        educationLevel: form.educationLevel,
                        profilePhotoUrl: profile?.profilePhotoUrl,
                        profilePhotoPublicId: profile?.profilePhotoPublicId,
                      });
                    }}
                    className="grid grid-cols-1 md:grid-cols-2 gap-4"
                  >
                    <div className="space-y-2">
                      <Label htmlFor="fullName">Full Name</Label>
                      <Input
                        id="fullName"
                        value={form.fullName}
                        onChange={(e) => setForm({ ...form, fullName: e.target.value })}
                        placeholder="Your full name"
                        required
                      />
                    </div>

                    <div className="space-y-2">
                      <Label htmlFor="username">Username</Label>
                      <Input
                        id="username"
                        value={profile?.username || ""}
                        disabled
                        className="bg-muted text-muted-foreground"
                      />
                    </div>

                    <div className="space-y-2">
                      <Label htmlFor="email">Email</Label>
                      <Input
                        id="email"
                        value={profile?.email || email || ""}
                        disabled
                        className="bg-muted text-muted-foreground"
                      />
                    </div>

                    <div className="space-y-2">
                      <Label htmlFor="phoneNumber">Phone Number</Label>
                      <Input
                        id="phoneNumber"
                        value={form.phoneNumber}
                        onChange={(e) => setForm({ ...form, phoneNumber: e.target.value })}
                        placeholder="+1 234 567 890"
                      />
                    </div>

                    <div className="space-y-2">
                      <Label htmlFor="gender">Gender</Label>
                      <Select
                        value={form.gender || "NOT_SPECIFIC"}
                        onValueChange={(val: any) => setForm({ ...form, gender: val })}
                      >
                        <SelectTrigger id="gender">
                          <SelectValue placeholder="Select gender" />
                        </SelectTrigger>
                        <SelectContent>
                          <SelectItem value="NOT_SPECIFIC">Not Specified</SelectItem>
                          <SelectItem value="MALE">Male</SelectItem>
                          <SelectItem value="FEMALE">Female</SelectItem>
                          <SelectItem value="OTHER">Other</SelectItem>
                        </SelectContent>
                      </Select>
                    </div>

                    <div className="space-y-2">
                      <Label htmlFor="dob">Date of Birth</Label>
                      <Input
                        id="dob"
                        type="date"
                        value={form.dateOfBirth}
                        onChange={(e) => setForm({ ...form, dateOfBirth: e.target.value })}
                      />
                    </div>

                    <div className="space-y-2 md:col-span-2">
                      <Label htmlFor="educationLevel" className="flex items-center gap-1.5">
                        <GraduationCap className="h-4 w-4" /> Education Level
                      </Label>
                      <Select
                        value={form.educationLevel || "NONE"}
                        onValueChange={(val) => setForm({ ...form, educationLevel: val === "NONE" ? "" : val })}
                      >
                        <SelectTrigger id="educationLevel">
                          <SelectValue placeholder="Select your highest education level" />
                        </SelectTrigger>
                        <SelectContent>
                          <SelectItem value="NONE">Not Specified</SelectItem>
                          <SelectItem value="High School">High School</SelectItem>
                          <SelectItem value="Associate Degree">Associate Degree</SelectItem>
                          <SelectItem value="Bachelor's Degree">Bachelor's Degree</SelectItem>
                          <SelectItem value="Master's Degree">Master's Degree</SelectItem>
                          <SelectItem value="Doctorate / PhD">Doctorate / PhD</SelectItem>
                          <SelectItem value="Self-Taught / Other">Self-Taught / Other</SelectItem>
                        </SelectContent>
                      </Select>
                    </div>

                    <div className="md:col-span-2 flex justify-end">
                      <Button type="submit" disabled={updateProfile.isPending}>
                        <Save className="mr-2 h-4 w-4" />
                        {updateProfile.isPending ? "Saving..." : "Save Profile"}
                      </Button>
                    </div>
                  </form>
                </>
              )}
            </CardContent>
          </Card>
        ) : (
          <Card>
            <CardHeader>
              <CardTitle className="flex items-center gap-2">
                <ShieldCheck className="h-5 w-5 text-primary" /> Account Role: {role}
              </CardTitle>
              <CardDescription>
                You are signed in with an administrative or teaching role ({email}).
              </CardDescription>
            </CardHeader>
          </Card>
        )}

        {/* Change Password Card */}
        <Card>
          <CardHeader>
            <CardTitle className="flex items-center gap-2">
              <Lock className="h-5 w-5 text-primary" /> Change Password
            </CardTitle>
            <CardDescription>Ensure your account stays secure by using a strong password.</CardDescription>
          </CardHeader>
          <CardContent>
            <ApiAlert error={pwError} />
            <form
              onSubmit={(e) => {
                e.preventDefault();
                changePw.mutate();
              }}
              className="space-y-4 max-w-md"
            >
              <div className="space-y-2">
                <Label htmlFor="oldPw">Current Password</Label>
                <Input
                  id="oldPw"
                  type="password"
                  required
                  value={pwForm.oldPassword}
                  onChange={(e) => setPwForm({ ...pwForm, oldPassword: e.target.value })}
                />
              </div>

              <div className="space-y-2">
                <Label htmlFor="newPw">New Password</Label>
                <Input
                  id="newPw"
                  type="password"
                  required
                  value={pwForm.newPassword}
                  onChange={(e) => setPwForm({ ...pwForm, newPassword: e.target.value })}
                />
              </div>

              <div className="space-y-2">
                <Label htmlFor="confirmPw">Confirm New Password</Label>
                <Input
                  id="confirmPw"
                  type="password"
                  required
                  value={pwForm.confirmPassword}
                  onChange={(e) => setPwForm({ ...pwForm, confirmPassword: e.target.value })}
                />
              </div>

              <Button type="submit" disabled={changePw.isPending}>
                {changePw.isPending ? "Updating Password..." : "Change Password"}
              </Button>
            </form>
          </CardContent>
        </Card>
      </main>
    </div>
  );
}
