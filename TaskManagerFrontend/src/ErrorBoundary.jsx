import {Component} from 'react';

export default class ErrorBoundary extends Component {
    state = {error: null};

    static getDerivedStateFromError(error) {
        return {error};
    }

    componentDidCatch(error, info) {
        console.error('Uncaught render error:', error, info);
    }

    handleReload = () => {
        this.setState({error: null});
        window.location.reload();
    };

    render() {
        if (this.state.error) {
            return (
                <div className="card">
                    <h1>Something went wrong</h1>
                    <p className="field-error">{this.state.error.message}</p>
                    <button type="button" onClick={this.handleReload}>Reload</button>
                </div>
            );
        }
        return this.props.children;
    }
}