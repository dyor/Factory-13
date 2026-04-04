# Polish & New Features Plan

## 1. iOS Captions Refinement (VideoUtils.ios.kt)
- [x] **Goal**: Increase the font size of the captions.
- [x] **Goal**: Dynamically adjust the background bounding box to fit the text width instead of spanning the entire screen.
- [x] **Goal**: Add rounded corners (`cornerRadius`) to the `CATextLayer` background so it feels less rigid.

## 2. Caption Dropdown in Editing Studio
- [x] **Goal**: Replace the simple "Captions" checkbox with a dropdown.
- [x] **Goal**: The dropdown options should be: `Top Captions`, `Bottom Captions`, and `No Captions`.
- [x] **Goal**: Update `EditingStudioViewModel` and `VideoUtils.ios.kt` to accept and process the chosen position (Top vs Bottom) and render the `CATextLayer` at the correct Y-coordinate.

## 3. Editing Studio UI Layout Adjustments
- [x] **Goal**: Move the new Captions dropdown to its own dedicated row above the final bottom navigation row.
- [x] **Goal**: Add an "Archive" button to the bottom row.
- [x] **Goal**: Make the bottom navigation row structure exactly match the Writer's Room: `[Back (←)] [Archive (↓)] [Publish (→)]`.

## 4. Editing Studio Timeline Interaction
- [x] **Goal**: Ensure the video is explicitly paused whenever the user taps on any of the timeline second-blocks (or when the fine-tune modal opens).

## 5. Recording Studio UI Layout Adjustments
- [x] **Goal**: Re-structure the bottom navigation row to match the Writer's Room and Editing Studio: `[Back (←)] [Archive (↓)] [Edit (→)]`.

## 6. Recording Studio "Clean Screen" Feature
- [x] **Goal**: While the video is actually recording or counting down, add a "Hide" button below the "Stop Early" button.
- [x] **Goal**: When "Hide" is tapped, hide *both* the "Hide" button and the "Stop Early" button to provide a perfectly clean camera view.
- [x] **Question for User**: If the user hides the "Stop Early" button, how should they stop the recording manually? Does it just automatically stop at the end of the duration, or should tapping anywhere on the screen un-hide the buttons?

## 7. Import Video to Editing Studio
- [x] **Goal**: Add an "Import Video" option in the Editing Studio (or perhaps a button to get there from the Home/Writer's room) to pick a pre-recorded video using a native file/photo picker.
- [x] **Question for User**: Where exactly should the "Import Video" button live? Should it be on the Home Screen as a standalone option, or should it be a button inside the Editing Studio that replaces the currently loaded active script video?

## 8. Theme and Button Layout Polish
- [x] **Goal**: Narrow back and archive buttons to give enough room for the Record/Publish buttons (with text). Ensure publish button isn't squished.
- [x] **Goal**: Create a theme element (or reusable component/modifier) to control the size of back, archive, and "action" buttons (publish, edit, etc.) consistently across the app.
- [x] **Goal**: Change "Import" to "Import New Video" and place it on its own row below play and reset in the Editing Studio. Ensure consistent spacing between these rows.

## 9. Captions Fixes (Positioning, Styling, Clipping)
- [x] **Goal**: Fix Bottom captions positioning. Currently it keeps them at the top even if bottom captions is selected, and top captions might show on top and bottom. Also it should be at the actual bottom (just a little padding) rather than 25% up the screen.
- [x] **Goal**: Add rounded edges to the captions background.
- [x] **Goal**: Fix text clipping in captions (add to another line if needed / dynamic width/height).

## 10. Import & Publish Improvements
- [x] **Goal**: Fix Import functionality. Currently it shows the first frame then shifts to a broken image when playing.
- [x] **Goal**: Add a circular progress indicator when Import or Publish are tapped, to indicate a long-running operation.
