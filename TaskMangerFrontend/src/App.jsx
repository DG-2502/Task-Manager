import {BrowserRouter, Routes, Route, Navigate} from 'react-router-dom';
import {AuthProvider} from './auth/AuthContext';
import ProtectedRoute from './auth/ProtectedRoute';
import LoginPage from './pages/LoginPage';
import TasksPage from './pages/TasksPage';
import {useEffect} from "react";
import RegisterPage from "./pages/RegisterPage.jsx";

export default function App() {
    console.log("App method is being run");
    useEffect(() => {
        console.log("Effects on App are being run");
    })
    return (
        <AuthProvider>
            <BrowserRouter>
                <Routes>
                    <Route path="/" element={<Navigate to="/tasks" replace/>}/>
                    <Route path="/login" element={<LoginPage/>}/>
                    <Route path="/register" element={<RegisterPage/>}/>
                    <Route
                        path="/tasks"
                        element={
                            <ProtectedRoute>
                                <TasksPage/>
                            </ProtectedRoute>
                        }
                    />
                </Routes>
            </BrowserRouter>
        </AuthProvider>
    );
}