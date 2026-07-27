import { render, screen } from '@testing-library/react'
import { describe, it, expect } from 'vitest'
import { ErrorMessage } from './ErrorMessage'

describe('ErrorMessage', () => {
  // The reason this component exists: two of the nine hand-written error
  // paragraphs had lost `text-danger`, so a failure rendered in ordinary ink.
  // Asserting the class here is asserting that the drift cannot happen again.
  it('always announces the failure and always looks like one', () => {
    render(<ErrorMessage error={new Error('Category "Groceries" is still in use')} />)

    const alert = screen.getByRole('alert')
    expect(alert).toHaveTextContent('Category "Groceries" is still in use')
    expect(alert).toHaveClass('text-danger')
  })

  it('takes spacing from the call site without losing the look', () => {
    render(<ErrorMessage error={new Error('Nope')} className="mt-3" />)

    expect(screen.getByRole('alert')).toHaveClass('text-danger', 'mt-3')
  })
})
