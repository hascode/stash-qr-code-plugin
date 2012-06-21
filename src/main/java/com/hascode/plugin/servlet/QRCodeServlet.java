package com.hascode.plugin.servlet;

import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.atlassian.stash.nav.NavBuilder;
import com.atlassian.stash.project.Project;
import com.atlassian.stash.project.ProjectService;
import com.atlassian.stash.repository.Repository;
import com.atlassian.stash.repository.RepositoryService;
import com.atlassian.stash.user.Permission;
import com.atlassian.stash.user.PermissionService;
import com.atlassian.stash.user.StashAuthenticationContext;
import com.atlassian.stash.user.StashUser;
import com.google.zxing.WriterException;
import com.hascode.plugin.components.CodeCreator;

import fj.data.Either;

public class QRCodeServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private final Logger log = LoggerFactory.getLogger(QRCodeServlet.class);
	private final CodeCreator codeCreator;
	private final PermissionService permissionService;
	private final StashAuthenticationContext authContext;
	private final RepositoryService repoService;
	private final NavBuilder navBuilder;
	private final ProjectService projectService;

	public QRCodeServlet(final CodeCreator codeCreator,
			final PermissionService permissionService,
			final StashAuthenticationContext authContext,
			final RepositoryService repoService, final NavBuilder navBuilder,
			final ProjectService projectService) {
		this.codeCreator = codeCreator;
		this.permissionService = permissionService;
		this.authContext = authContext;
		this.repoService = repoService;
		this.navBuilder = navBuilder;
		this.projectService = projectService;
	}

	@Override
	protected void doGet(final HttpServletRequest req,
			final HttpServletResponse res) throws ServletException, IOException {
		final String project = req.getParameter("p");
		final String repository = req.getParameter("r");

		if (StringUtils.isEmpty(project) && StringUtils.isEmpty(repository)) {
			log.warn("we need either a project or a repository here .. quitting");
			signalError(res);
			return;
		}

		final Either<Exception, BufferedImage> result = handleProjectOrRepository(
				project, repository);
		if (result.isLeft()) {
			log.warn("no image created - reason is: "
					+ result.left().value().getMessage(), result.left().value());
			signalError(res);
			return;
		}

		sendPngImage(res, result.right().value());
	}

	private Either<Exception, BufferedImage> handleProjectOrRepository(
			final String project, final String repository) {
		try {
			final StashUser user = authContext.getCurrentUser();

			// handle project
			if (!StringUtils.isEmpty(project)) {
				return handleProject(project, user);
			}
			// handle repository
			else {
				return handleRepository(repository, user);

			}
		} catch (Exception e) {
			return Either.left(e);
		}
	}

	private Either<Exception, BufferedImage> handleProject(
			final String project, final StashUser user) throws Exception {
		if (StringUtils.isEmpty(project)) {
			throw new Exception("no project given");
		}

		final Project proj = projectService.getById(NumberUtils
				.createInteger(project));
		if (null == proj) {
			throw new Exception("no project for given key: " + project);
		}

		if (null == user
				|| !permissionService.hasProjectPermission(user, proj,
						Permission.PROJECT_READ)) {
			throw new Exception("user has no permission to view this project");
		}

		final String url = createUrl(proj);

		final BufferedImage img = codeCreator.createURLCode(url);
		if (img == null) {
			throw new Exception("created image was null");
		}
		return Either.right(img);
	}

	private Either<Exception, BufferedImage> handleRepository(
			final String repository, final StashUser user) throws Exception,
			WriterException, IOException {
		if (StringUtils.isEmpty(repository)) {
			throw new Exception("no repository given");
		}

		final Repository repo = repoService.findRepositoryById(NumberUtils
				.createInteger(repository));
		if (null == repo) {
			throw new Exception("no repository for given id: " + repository);
		}

		if (null == user
				|| !permissionService.hasRepositoryPermission(user, repo,
						Permission.REPO_READ)) {
			throw new Exception(
					"user has no permission to view this repository");
		}

		final String url = createUrl(repo);

		final BufferedImage img = codeCreator.createURLCode(url);
		if (img == null) {
			throw new Exception("created image was null");
		}
		return Either.right(img);
	}

	private String createUrl(final Repository repo) {
		return navBuilder.repo(repo).buildAbsolute();
	}

	private String createUrl(final Project proj) {
		return navBuilder.project(proj).buildAbsolute();
	}

	private void signalError(final HttpServletResponse res) {
		log.debug("sending error status");
		res.setStatus(404);
	}

	private void sendPngImage(final HttpServletResponse res,
			final BufferedImage image) throws IOException {
		res.setContentType("image/png");
		ImageIO.write(image, "PNG", res.getOutputStream());
	}

}