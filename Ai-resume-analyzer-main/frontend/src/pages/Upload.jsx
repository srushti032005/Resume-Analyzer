import { useState, useCallback } from 'react';
import { useDropzone } from 'react-dropzone';
import { useNavigate } from 'react-router-dom';
import toast from 'react-hot-toast';
import { motion } from 'framer-motion';
import { UploadCloud, FileCheck2, Loader2, X } from 'lucide-react';
import { resumeApi } from '../api/services.js';

export default function Upload() {
  const navigate = useNavigate();
  const [file, setFile] = useState(null);
  const [progress, setProgress] = useState(0);
  const [uploading, setUploading] = useState(false);

  const onDrop = useCallback((accepted, rejected) => {
    if (rejected?.length) {
      toast.error('Only PDF files up to 5MB are accepted');
      return;
    }
    setFile(accepted[0]);
  }, []);

  const { getRootProps, getInputProps, isDragActive } = useDropzone({
    onDrop,
    accept: { 'application/pdf': ['.pdf'] },
    maxSize: 5 * 1024 * 1024,
    maxFiles: 1,
  });

  const handleUpload = async () => {
    if (!file) return;
    setUploading(true);
    setProgress(0);
    try {
      const res = await resumeApi.upload(file, setProgress);
      toast.success('Resume uploaded — analysis started');
      navigate(`/app/resumes/${res.data.data.id}`);
    } catch (err) {
      toast.error(err.response?.data?.message || 'Upload failed');
    } finally {
      setUploading(false);
    }
  };

  return (
    <div className="mx-auto max-w-2xl">
      <h1 className="font-display text-3xl font-bold">Upload Resume</h1>
      <p className="text-current/50 mt-1 mb-8">
        Upload a PDF resume to receive an instant AI-powered analysis.
      </p>

      <div
        {...getRootProps()}
        className={`card cursor-pointer border-2 border-dashed p-12 text-center transition ${
          isDragActive
            ? 'border-brand-500 bg-brand-500/5'
            : 'border-white/15 hover:border-brand-500/50'
        }`}
      >
        <input {...getInputProps()} />
        <motion.div
          animate={{ y: isDragActive ? -6 : 0 }}
          className="flex flex-col items-center gap-4"
        >
          <div className="rounded-2xl bg-brand-500/15 p-5 text-brand-400">
            <UploadCloud size={36} />
          </div>
          <div>
            <p className="font-display text-lg font-semibold">
              {isDragActive
                ? 'Drop your resume here'
                : 'Drag & drop your resume'}
            </p>
            <p className="text-sm text-current/40 mt-1">
              or click to browse — PDF only, max 5MB
            </p>
          </div>
        </motion.div>
      </div>

      {file && (
        <motion.div
          initial={{ opacity: 0, y: 12 }}
          animate={{ opacity: 1, y: 0 }}
          className="card mt-5 flex items-center justify-between p-4"
        >
          <div className="flex items-center gap-3">
            <div className="rounded-lg bg-emerald-500/15 p-2.5 text-emerald-400">
              <FileCheck2 size={20} />
            </div>
            <div>
              <p className="text-sm font-medium truncate max-w-[220px]">
                {file.name}
              </p>
              <p className="text-xs text-current/40">
                {(file.size / 1024).toFixed(0)} KB
              </p>
            </div>
          </div>
          {!uploading && (
            <button
              onClick={() => setFile(null)}
              className="btn-ghost p-2"
              aria-label="Remove file"
            >
              <X size={16} />
            </button>
          )}
        </motion.div>
      )}

      {uploading && (
        <div className="mt-5">
          <div className="h-2.5 w-full overflow-hidden rounded-full bg-white/10">
            <motion.div
              className="h-full bg-brand-500"
              animate={{ width: `${progress}%` }}
            />
          </div>
          <p className="mt-2 text-xs text-current/50">
            Uploading… {progress}%
          </p>
        </div>
      )}

      <button
        onClick={handleUpload}
        disabled={!file || uploading}
        className="btn-primary mt-6 w-full py-3"
      >
        {uploading ? (
          <>
            <Loader2 size={18} className="animate-spin" /> Analyzing…
          </>
        ) : (
          <>
            <UploadCloud size={18} /> Analyze resume
          </>
        )}
      </button>
    </div>
  );
}
