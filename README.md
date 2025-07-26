<h1>RandoChat 📱</h1>
<p>A modern, real-time anonymous chat app built with <strong>Kotlin</strong>, <strong>Jetpack Compose</strong>, <strong>MVVM architecture</strong>, and <strong>Firebase</strong>.</p>

<h2>🔥 Features</h2>
<ul>
  <li><strong>🧑‍🤝‍🧑 Anonymous Random Chat</strong><br />
    Instantly connect with a random stranger for a one-on-one chat.<br />
    Supports chat types: <strong>Random</strong> (implemented), with future expansion for <em>Age-based</em> and <em>Location-based</em> matching.
  </li>
  <li><strong>💬 Real-Time Messaging</strong><br />
    Text, image, and voice messages with delivery status (sending, sent, seen).<br />
    Lazy loading, typing indicators, and timestamp grouping included.
  </li>
  <li><strong>🔐 End-to-End Message Encryption</strong><br />
    Messages are AES encrypted before saving to Firebase, decrypted on receive.
  </li>
  <li><strong>🔄 Smart Matching Engine</strong><br />
    Uses Firebase transactions for safe and fair user matching.
  </li>
  <li><strong>👤 Authentication & Onboarding</strong><br />
    Email/Password and Google Sign-In support with animated splash + welcome flow.
  </li>
  <li><strong>📥 Offline Caching</strong><br />
    User state and message history cached locally for fast recovery.
  </li>
  <li><strong>🔔 Push Notifications</strong><br />
    Firebase Cloud Messaging sends real-time notifications for new messages.
  </li>
  <li><strong>🧪 UX Enhancements</strong><br />
    Single-click debounce, elegant form validations, error toasts, and progress UI.
  </li>
</ul>

<h2>🧱 Tech Stack</h2>
<table>
  <thead>
    <tr>
      <th>Layer</th>
      <th>Technologies Used</th>
    </tr>
  </thead>
  <tbody>
    <tr><td>UI</td><td>Jetpack Compose, Material3, Lottie</td></tr>
    <tr><td>Architecture</td><td>MVVM, Kotlin Coroutines, Flow</td></tr>
    <tr><td>Auth & DB</td><td>Firebase Auth, Realtime Database</td></tr>
    <tr><td>Notifications</td><td>Firebase Cloud Messaging (FCM)</td></tr>
    <tr><td>Utilities</td><td>AES Encryption, SharedPreferences, Timber</td></tr>
    <tr><td>Navigation</td><td>Jetpack Navigation Compose</td></tr>
  </tbody>
</table>

<h2>📁 Project Structure</h2>
<ul>
  <li><code>ui/screen/</code> – Jetpack Compose UI screens</li>
  <li><code>repository/</code> – Business logic, Firebase handling</li>
  <li><code>service/</code> – FCM, local prefs, background tasks</li>
  <li><code>utils/</code> – Encryption, validators, constants</li>
  <li><code>model/</code> – Data models (User, Message, ChatRoom)</li>
  <li><code>navigation/</code> – Navigation graph</li>
</ul>

<h2>🚀 How to Run</h2>
<ol>
  <li>Clone the repository</li>
  <li>Add your <code>google-services.json</code></li>
  <li>Enable Firebase Auth & Realtime Database</li>
  <li>Open with Android Studio (API 21+)</li>
</ol>

<h2>✅ TODO & Future Plans</h2>
<ul>
  <li>[ ] Age & Location-based matching</li>
  <li>[ ] Voice & image storage via Firebase Storage</li>
  <li>[ ] Report/block users</li>
  <li>[ ] Dark mode</li>
  <li>[ ] In-app settings & preferences</li>
</ul>

<h2>🧠 Why This Project?</h2>
<p>
RandoChat demonstrates:
<ul>
  <li>Real-time scalable communication</li>
  <li>Modern Compose UI practices</li>
  <li>Firebase ecosystem expertise</li>
  <li>Secure messaging with encryption</li>
  <li>Clean MVVM and offline-first design</li>
</ul>
</p>
