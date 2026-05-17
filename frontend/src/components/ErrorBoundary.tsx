import { Component, type ErrorInfo, type ReactNode } from 'react';

type Props = {
  children: ReactNode;
};

type State = {
  hasError: boolean;
};

export class ErrorBoundary extends Component<Props, State> {
  state: State = { hasError: false };

  static getDerivedStateFromError(): State {
    return { hasError: true };
  }

  componentDidCatch(error: Error, errorInfo: ErrorInfo) {
    console.error('Unhandled app error', error, errorInfo);
  }

  render() {
    if (this.state.hasError) {
      return (
        <main className="app-shell">
          <section className="app-error" role="alert">
            <p className="eyebrow">Verifier error</p>
            <h1>Something went wrong</h1>
            <p>Refresh the page and try the verification again.</p>
          </section>
        </main>
      );
    }

    return this.props.children;
  }
}
