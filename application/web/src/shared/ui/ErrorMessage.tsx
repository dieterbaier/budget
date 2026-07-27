interface Props {
  error: Error
  /** Spacing is the caller's business; the look is not. */
  className?: string
}

/**
 * The one way a failure is shown. It was previously written out at nine call
 * sites, and they had already drifted: two of them rendered without
 * `text-danger`, so a failed load on the categories page appeared in ordinary
 * ink while every other error in the app was red. That is the kind of thing
 * hand-copied markup does quietly.
 */
export function ErrorMessage({ error, className }: Props) {
  return (
    <p role="alert" className={className ? `text-danger ${className}` : 'text-danger'}>
      {error.message}
    </p>
  )
}
