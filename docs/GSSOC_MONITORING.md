# GSSoC Issue Monitoring System

This documentation explains how the Girl Script Summer of Code (GSSoC) issue monitoring system works in the Pixelpholio repository.

## Overview

The GSSoC monitoring system automatically tracks and categorizes issues labeled for GSSoC participation, maintaining an up-to-date list in the README.md file.

## Components

### 1. README.md Section
- **Location**: `## 🌟 GSSoC (Girl Script Summer of Code) Contributions`
- **Purpose**: Displays categorized lists of GSSoC issues
- **Categories**: 
  - 🟢 Beginner-Friendly Issues
  - 🟡 Intermediate Issues
  - 🔴 Advanced Issues
- **Statistics**: Real-time counts of total, completed, open issues and contributors

### 2. Update Script (`scripts/update-gssoc-issues.py`)
- **Purpose**: Fetches GSSoC issues from GitHub API and updates README
- **Language**: Python 3
- **Dependencies**: `requests` library
- **Features**:
  - Fetches issues with GSSoC labels
  - Categorizes by difficulty level
  - Calculates statistics
  - Updates README with formatted markdown

### 3. Shell Script (`scripts/update-gssoc.sh`)
- **Purpose**: Convenient wrapper for running the Python updater
- **Features**:
  - Dependency checking
  - Automatic package installation
  - User-friendly output

### 4. GitHub Actions Workflow (`.github/workflows/update-gssoc-tracker.yml`)
- **Purpose**: Automated updates when issues change
- **Triggers**:
  - Issue events (opened, closed, labeled, etc.)
  - Daily schedule (9 AM UTC)
  - Manual workflow dispatch
- **Features**:
  - Automatic README updates
  - Git commits with structured messages
  - No-change detection

## Configuration

### GSSoC Labels
The system recognizes these labels as GSSoC-related:
- `gssoc`
- `gssoc24`
- `girl-script`

### Difficulty Classification
Issues are categorized based on these label patterns:

**Beginner**:
- `good-first-issue`
- `beginner-friendly`
- `easy`

**Intermediate** (default):
- `intermediate`
- `medium`

**Advanced**:
- `advanced`
- `hard`
- `difficult`

## Usage

### Manual Update
```bash
# Option 1: Use the shell script
./scripts/update-gssoc.sh

# Option 2: Run Python script directly
python3 scripts/update-gssoc-issues.py

# Option 3: With GitHub token for API access
GITHUB_TOKEN=your_token python3 scripts/update-gssoc-issues.py
```

### Automatic Updates
The GitHub Actions workflow automatically:
1. Monitors issue changes
2. Updates the README when GSSoC issues are modified
3. Commits changes with descriptive messages
4. Runs daily to catch any missed updates

## Adding GSSoC Issues

To add a new GSSoC issue:

1. **Create the issue** with a clear title and description
2. **Add GSSoC label**: `gssoc`, `gssoc24`, or `girl-script`
3. **Add difficulty label**: Choose from beginner/intermediate/advanced labels
4. **Add category labels**: `bug`, `enhancement`, `documentation`, etc.

Example issue labels:
```
gssoc, good-first-issue, documentation, help-wanted
```

## Monitoring and Maintenance

### Daily Checks
- Automated workflow runs daily at 9 AM UTC
- Manual trigger available via GitHub Actions tab

### Manual Monitoring
- Check README.md for current statistics
- Review issue lists for accuracy
- Verify categorization is correct

### Troubleshooting

**Issue not showing up:**
- Verify GSSoC label is applied
- Check if issue is in correct repository
- Run manual update to force refresh

**Wrong category:**
- Adjust difficulty labels on the issue
- Re-run the update script

**API rate limits:**
- Set `GITHUB_TOKEN` environment variable
- Wait for rate limit reset (typically 1 hour)

## Contributing to the System

### Modifying Categories
Edit `DIFFICULTY_LABELS` in `scripts/update-gssoc-issues.py`:

```python
DIFFICULTY_LABELS = {
    "beginner": ["good-first-issue", "beginner-friendly", "easy"],
    "intermediate": ["intermediate", "medium"],
    "advanced": ["advanced", "hard", "difficult"]
}
```

### Adding New GSSoC Labels
Edit `GSSOC_LABELS` in the same file:

```python
GSSOC_LABELS = ["gssoc", "gssoc24", "girl-script", "new-label"]
```

### Customizing README Format
Modify the `format_issue_list()` function to change how issues are displayed.

## Security Considerations

- GitHub token is handled securely via environment variables
- No sensitive data is logged or stored
- API requests use appropriate rate limiting
- Automated commits use GitHub Actions built-in authentication

## Future Enhancements

Potential improvements:
- Integration with GSSoC official tracking systems
- Contributor leaderboards
- Issue completion metrics
- Automated issue templates for GSSoC
- Integration with project management tools

## Support

For issues with the monitoring system:
1. Check GitHub Actions workflow logs
2. Run manual update to test functionality
3. Create an issue with the `bug` and `gssoc` labels
4. Tag repository maintainers

---

*This system helps maintain transparency and organization for GSSoC participants and mentors.*